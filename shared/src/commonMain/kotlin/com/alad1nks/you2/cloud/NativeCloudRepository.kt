package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.CloudState
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.data.repository.WishDocument
import com.alad1nks.you2.core.model.Couple
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.User
import com.alad1nks.you2.core.model.WishDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class NativeCloudRepository(
    private val provider: NativeCloudProvider,
    private val auth: AuthClient,
    private val dates: DateSource,
) : CloudWishesRepository {
    private val empty = PairedSpace(Couple(User("", ""), User("", "")), emptyList())
    private val mutableSpace = MutableStateFlow(empty)
    override val space = mutableSpace.asStateFlow()
    private val state = MutableStateFlow(CloudState())
    override val cloudState = state.asStateFlow()
    private var generation = 0
    private var closed = false
    private var coupleId: String? = null
    private var unsubscribe: (() -> Unit)? = null
    private var retryWrite: (() -> Unit)? = null
    private val unsubscribeAuth = auth.observeUser { connect() }

    private fun connect() {
        if (closed) return
        val token = ++generation
        unsubscribe?.invoke()
        unsubscribe = null
        coupleId = null
        retryWrite = null
        mutableSpace.value = empty
        val uid = auth.currentUser?.id
        state.value = CloudState(userId = uid)
        if (uid == null) return
        unsubscribe =
            provider.observe(uid) { snapshot ->
                if (current(token)) receive(uid, snapshot)
            }
    }

    private fun current(token: Int) = !closed && generation == token && auth.currentUser?.id == state.value.userId

    private fun receive(uid: String, snapshot: NativeCloudSnapshot) {
        try {
            val phase = CloudPhase.valueOf(snapshot.phase.name)
            mutableSpace.value =
                if (phase == CloudPhase.READY) {
                    require(snapshot.displayName.isNotBlank() && !snapshot.partnerName.isNullOrBlank())
                    require(!snapshot.partnerId.isNullOrBlank() && snapshot.partnerId != uid)
                    require(!snapshot.coupleId.isNullOrBlank())
                    PairedSpace(
                        Couple(User(uid, snapshot.displayName), User(snapshot.partnerId, snapshot.partnerName)),
                        snapshot.wishes.map { WishDocument.decode(it.id, it.fields) },
                    )
                } else {
                    empty
                }
            coupleId = snapshot.coupleId
            state.update {
                it.copy(
                    displayName = snapshot.displayName,
                    phase = phase,
                    inviteCode = snapshot.inviteCode,
                    fromCache = snapshot.fromCache,
                    pendingWrites = snapshot.pendingWrites,
                    failure = snapshot.failure?.shared() ?: it.failure,
                )
            }
            if (phase == CloudPhase.ERROR) stopFailedSession(snapshot.failure?.shared() ?: CloudFailure.UNKNOWN)
        } catch (_: Exception) {
            stopFailedSession(CloudFailure.DATA)
        }
    }

    private fun stopFailedSession(failure: CloudFailure) {
        generation++
        unsubscribe?.invoke()
        unsubscribe = null
        mutableSpace.value = empty
        coupleId = null
        retryWrite = null
        state.update { it.copy(phase = CloudPhase.ERROR, failure = failure, busy = false, pendingWrites = false) }
    }

    override fun createInvitation(name: String) =
        write("invite", provider.newId(), mapOf("name" to cleanName(name), "pairId" to (coupleId ?: provider.newId())))

    override fun joinInvitation(name: String, code: String) {
        val clean = code.trim().lowercase()
        if (!Regex("[a-f0-9]{32}").matches(clean)) {
            state.update { it.copy(failure = CloudFailure.INVITE) }
            return
        }
        write("join", clean, mapOf("name" to cleanName(name)))
    }

    override fun create(draft: WishDraft): String {
        val id = provider.newId()
        write("create", id, mapOf("content" to WishDocument.content(draft), "createdOn" to dates.today().toString()))
        return id
    }

    override fun edit(id: String, draft: WishDraft) = write("edit", id, mapOf("content" to WishDocument.content(draft)))

    override fun complete(id: String) = write("complete", id, mapOf("date" to dates.today().toString()))

    override fun restore(id: String) = write("restore", id)

    override fun editMemory(id: String, memory: String) {
        require(memory.length <= 500)
        write("memory", id, mapOf("memory" to memory))
    }

    override fun delete(id: String) = write("delete", id)

    override fun renameUser(name: String) = write("rename", "", mapOf("name" to cleanName(name)))

    private fun cleanName(name: String): String = name.trim().also { require(it.isNotEmpty() && it.length <= 80) }

    private fun write(operation: String, id: String, fields: Map<String, Any?> = emptyMap()) {
        val uid = state.value.userId ?: return
        if (closed || state.value.busy || state.value.phase == CloudPhase.ERROR) return
        if (operation in listOf("create", "edit", "complete", "restore", "memory", "delete") &&
            state.value.phase != CloudPhase.READY
        ) {
            return
        }
        val token = generation
        val pair = coupleId
        retryWrite = { if (current(token) && coupleId == pair) write(operation, id, fields) }
        state.update { it.copy(busy = true, failure = null) }
        provider.write(operation, uid, pair, id, fields) { failure ->
            if (current(token)) {
                if (failure == null) retryWrite = null
                state.update { it.copy(busy = false, failure = failure?.shared()) }
            }
        }
    }

    override fun retry() {
        if (state.value.busy || closed) return
        if (state.value.phase == CloudPhase.ERROR) connect() else retryWrite?.invoke() ?: dismissFailure()
    }

    override fun dismissFailure() {
        retryWrite = null
        state.update { it.copy(failure = null) }
    }

    override fun close() {
        closed = true
        generation++
        unsubscribeAuth()
        unsubscribe?.invoke()
        unsubscribe = null
        retryWrite = null
        coupleId = null
        mutableSpace.value = empty
        state.value = CloudState()
    }
}

private fun NativeCloudFailure.shared() = CloudFailure.valueOf(name)
