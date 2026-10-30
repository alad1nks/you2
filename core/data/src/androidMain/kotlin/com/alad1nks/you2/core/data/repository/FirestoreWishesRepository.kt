package com.alad1nks.you2.core.data.repository

import com.alad1nks.you2.core.model.Couple
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.User
import com.alad1nks.you2.core.model.WishDraft
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

/** All SDK listeners and task completions are delivered on Android's main thread. */
class FirestoreWishesRepository(private val dateSource: DateSource) : CloudWishesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val empty = PairedSpace(Couple(User("", ""), User("", "")), emptyList())
    private val mutableSpace = MutableStateFlow(empty)
    override val space = mutableSpace.asStateFlow()
    private val state = MutableStateFlow(CloudState())
    override val cloudState = state.asStateFlow()
    private val listeners = mutableListOf<ListenerRegistration>()
    private val pairListeners = mutableListOf<ListenerRegistration>()
    private var generation = 0
    private var coupleId: String? = null
    private var retryWrite: (() -> Unit)? = null
    private var closed = false
    private val authListener = FirebaseAuth.AuthStateListener { connect() }

    init {
        auth.addAuthStateListener(authListener)
    }

    private fun connect() {
        if (closed) return
        generation++
        val token = generation
        listeners.forEach { it.remove() }
        listeners.clear()
        clearPair()
        retryWrite = null
        mutableSpace.value = empty
        val user = auth.currentUser
        state.value = CloudState(userId = user?.uid)
        if (user == null) return
        val profile = db.collection("users").document(user.uid)
        var creatingProfile = false
        listeners +=
            profile.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (!current(token)) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    fail(error, fatal = true)
                } else if (!snapshot.exists()) {
                    // Never infer a new account from a cache miss. Existing accounts can load offline.
                    if (!snapshot.metadata.isFromCache && !creatingProfile) {
                        creatingProfile = true
                        db.runTransaction { transaction ->
                            if (!transaction.get(profile).exists()) {
                                transaction.set(profile, mapOf("name" to "", "coupleId" to null))
                            }
                        }.addOnFailureListener { failure ->
                            if (current(token)) fail(failure, fatal = true)
                        }
                    }
                } else {
                    state.update { it.copy(displayName = snapshot.getString("name").orEmpty()) }
                    val id = snapshot.getString("coupleId")
                    if (id == null) {
                        clearPair()
                        state.update { it.copy(phase = CloudPhase.UNPAIRED, inviteCode = null) }
                    } else if (id != coupleId) {
                        observePair(id, user.uid, token)
                    }
                }
            }
    }

    private fun current(token: Int) = !closed && generation == token && auth.currentUser?.uid == state.value.userId

    private fun clearPair() {
        pairListeners.forEach { it.remove() }
        pairListeners.clear()
        coupleId = null
        mutableSpace.value = empty
    }

    private fun observePair(id: String, uid: String, token: Int) {
        clearPair()
        coupleId = id
        state.update { it.copy(phase = CloudPhase.LOADING) }
        var observingMembers = false
        pairListeners +=
            db.collection("couples").document(id).addSnapshotListener { snapshot, error ->
                if (!current(token) || coupleId != id) return@addSnapshotListener
                if (error != null || snapshot == null || !snapshot.exists()) {
                    fail(error, fatal = true)
                    return@addSnapshotListener
                }
                val members = (snapshot.get("memberIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
                if (uid !in members || members.size !in 1..2) {
                    fail(null, fatal = true, failure = CloudFailure.DATA)
                } else if (members.size == 1) {
                    state.update { it.copy(phase = CloudPhase.UNPAIRED, inviteCode = snapshot.getString("inviteId")) }
                } else if (!observingMembers) {
                    observingMembers = true
                    observeContent(id, uid, members.single { it != uid }, token)
                }
            }
    }

    private fun observeContent(id: String, uid: String, partnerId: String, token: Int) {
        var you: User? = null
        var partner: User? = null
        var wishesLoaded = false

        fun publish() {
            val me = you ?: return
            val other = partner ?: return
            if (!wishesLoaded || !current(token) || state.value.phase == CloudPhase.ERROR) return
            mutableSpace.update { it.copy(couple = Couple(me, other)) }
            state.update { it.copy(phase = CloudPhase.READY, inviteCode = null) }
        }
        for (member in listOf(uid, partnerId)) {
            pairListeners +=
                db.collection("users").document(member).addSnapshotListener { snapshot, error ->
                    if (!current(token) || coupleId != id) return@addSnapshotListener
                    val name = snapshot?.getString("name")
                    if (error != null || name.isNullOrBlank()) {
                        fail(error, fatal = true)
                    } else {
                        if (member == uid) you = User(uid, name) else partner = User(partnerId, name)
                        publish()
                    }
                }
        }
        pairListeners +=
            db.collection("couples").document(id).collection("wishes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                    if (!current(token) || coupleId != id) return@addSnapshotListener
                    if (error != null || snapshot == null) {
                        fail(error, fatal = true)
                        return@addSnapshotListener
                    }
                    try {
                        val wishes = snapshot.documents.map { WishDocument.decode(it.id, requireNotNull(it.data)) }
                        mutableSpace.update { it.copy(wishes = wishes) }
                        wishesLoaded = true
                        state.update {
                            it.copy(
                                fromCache = snapshot.metadata.isFromCache,
                                pendingWrites = snapshot.metadata.hasPendingWrites(),
                            )
                        }
                        publish()
                    } catch (_: IllegalArgumentException) {
                        fail(null, fatal = true, failure = CloudFailure.DATA)
                    } catch (_: IllegalStateException) {
                        fail(null, fatal = true, failure = CloudFailure.DATA)
                    } catch (_: ClassCastException) {
                        fail(null, fatal = true, failure = CloudFailure.DATA)
                    } catch (_: NullPointerException) {
                        fail(null, fatal = true, failure = CloudFailure.DATA)
                    }
                }
    }

    override fun createInvitation(name: String) {
        val uid = state.value.userId ?: return
        val cleanName = name.trim()
        require(cleanName.isNotEmpty() && cleanName.length <= 80)
        val id = coupleId ?: db.collection("couples").document().id
        val code = UUID.randomUUID().toString().replace("-", "")
        val pair = db.collection("couples").document(id)
        val profile = db.collection("users").document(uid)
        val invite = db.collection("invites").document(code)
        write {
            db.runTransaction { tx ->
                val existingProfile = tx.get(profile)
                val existingPair = tx.get(pair)
                check(existingProfile.getString("coupleId") == null || existingProfile.getString("coupleId") == id)
                if (existingPair.exists()) {
                    check(existingPair.get("memberIds") == listOf(uid))
                    tx.update(pair, "inviteId", code)
                } else {
                    tx.set(
                        pair,
                        mapOf(
                            "memberIds" to listOf(uid),
                            "inviteId" to code,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }
                tx.update(profile, mapOf("name" to cleanName, "coupleId" to id))
                tx.set(
                    invite,
                    mapOf(
                        "ownerId" to uid,
                        "coupleId" to id,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "expiresAt" to Timestamp(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)),
                        "usedBy" to null,
                    ),
                )
            }
        }
    }

    override fun joinInvitation(name: String, code: String) {
        val uid = state.value.userId ?: return
        val cleanName = name.trim()
        val cleanCode = code.trim().lowercase()
        require(cleanName.isNotEmpty() && cleanName.length <= 80)
        if (!Regex("[a-f0-9]{32}").matches(cleanCode)) {
            fail(null, failure = CloudFailure.INVITE)
            return
        }
        write {
            val invite = db.collection("invites").document(cleanCode)
            val profile = db.collection("users").document(uid)
            db.runTransaction { tx ->
                val invitation = tx.get(invite)
                val me = tx.get(profile)
                check(me.getString("coupleId") == null)
                check(
                    invitation.exists() && invitation.getString(
                        "ownerId",
                    ) != uid && invitation.getString("usedBy") == null,
                )
                val id = requireNotNull(invitation.getString("coupleId"))
                // The invite is the capability. A non-member cannot read the couple before joining.
                tx.update(db.collection("couples").document(id), "memberIds", FieldValue.arrayUnion(uid))
                tx.update(profile, mapOf("name" to cleanName, "coupleId" to id))
                tx.update(invite, "usedBy", uid)
            }
        }
    }

    override fun create(draft: WishDraft): String {
        val uid = requireNotNull(state.value.userId)
        val document = wishes().document()
        val data =
            mapOf(
                "authorId" to uid,
                "createdOn" to dateSource.today().toString(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "content" to WishDocument.content(draft),
                "completion" to null,
            )
        write { document.set(data) }
        return document.id
    }

    override fun edit(id: String, draft: WishDraft) = updateWish(id, mapOf("content" to WishDocument.content(draft)))

    override fun complete(id: String) {
        val document = wishes().document(id)
        val completion =
            mapOf(
                "date" to dateSource.today().toString(),
                "authorId" to requireNotNull(state.value.userId),
                "memory" to "",
            )
        write {
            db.runTransaction { tx ->
                val wish = tx.get(document)
                check(wish.exists())
                // Concurrent completion must not replace the first author's memory.
                if (wish.get("completion") == null) {
                    tx.update(document, mapOf("completion" to completion, "updatedAt" to FieldValue.serverTimestamp()))
                }
            }
        }
    }

    override fun restore(id: String) = updateWish(id, mapOf("completion" to null))

    override fun editMemory(id: String, memory: String) {
        require(memory.length <= 500)
        updateWish(id, mapOf("completion.memory" to memory))
    }

    override fun delete(id: String) {
        val document = wishes().document(id)
        write { document.delete() }
    }

    override fun renameUser(name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty() && cleanName.length <= 80)
        val document = db.collection("users").document(requireNotNull(state.value.userId))
        write { document.update("name", cleanName) }
    }

    private fun wishes() = db.collection("couples").document(requireNotNull(coupleId)).collection("wishes")

    private fun updateWish(id: String, fields: Map<String, Any?>) {
        val document = wishes().document(id)
        write { document.update(fields + ("updatedAt" to FieldValue.serverTimestamp())) }
    }

    private fun write(operation: () -> Task<*>) {
        if (state.value.busy || state.value.userId == null) return
        val token = generation
        retryWrite = { if (current(token)) write(operation) }
        state.update { it.copy(busy = true, failure = null) }
        operation().addOnCompleteListener { task ->
            if (!current(token)) return@addOnCompleteListener
            state.update { it.copy(busy = false) }
            if (task.isSuccessful) retryWrite = null else fail(task.exception)
        }
    }

    private fun fail(error: Exception?, fatal: Boolean = false, failure: CloudFailure? = null) {
        val reason =
            failure ?: when ((error as? FirebaseFirestoreException)?.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    CloudFailure.ACCESS
                }

                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                -> {
                    CloudFailure.NETWORK
                }

                else -> {
                    if (error is IllegalStateException || error is IllegalArgumentException) {
                        if (state.value.phase == CloudPhase.UNPAIRED) CloudFailure.INVITE else CloudFailure.DATA
                    } else {
                        CloudFailure.UNKNOWN
                    }
                }
            }
        if (fatal) {
            generation++
            listeners.forEach { it.remove() }
            listeners.clear()
            clearPair()
            retryWrite = null
            state.update { it.copy(phase = CloudPhase.ERROR, failure = reason, busy = false, pendingWrites = false) }
        } else {
            state.update { it.copy(failure = reason) }
        }
    }

    override fun retry() {
        if (state.value.busy) return
        if (state.value.phase == CloudPhase.ERROR) connect() else retryWrite?.invoke() ?: dismissFailure()
    }

    override fun dismissFailure() {
        retryWrite = null
        state.update { it.copy(failure = null) }
    }

    override fun close() {
        closed = true
        generation++
        auth.removeAuthStateListener(authListener)
        listeners.forEach { it.remove() }
        listeners.clear()
        clearPair()
        retryWrite = null
        state.value = CloudState()
    }
}
