package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.DesktopAuthFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.time.Instant
import java.util.UUID

/** Main-thread lifecycle and callbacks, bounded IO requests, atomic writes under the user's rules. */
internal class DesktopCloudProvider(
    private val auth: AuthClient,
    private val tokenFor: suspend (String) -> String,
    private val api: DesktopFirestoreApi = DesktopFirestoreApi(),
    private val pollMillis: Long = 15_000,
) : NativeCloudProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val operations = Mutex()
    private var wake: Channel<Unit>? = null
    private var generation = 0

    override fun newId(): String = UUID.randomUUID().toString().replace("-", "")

    override fun observe(userId: String, onChange: (NativeCloudSnapshot) -> Unit): () -> Unit {
        val session = ++generation
        val refresh = Channel<Unit>(Channel.CONFLATED)
        wake = refresh
        val job =
            scope.launch {
                try {
                    while (auth.currentUser?.id == userId && generation == session) {
                        operations.withLock {
                            val snapshot = transaction(userId, session) { snapshot(userId) }
                            if (auth.currentUser?.id == userId && generation == session) onChange(snapshot)
                        }
                        withTimeoutOrNull(pollMillis) { refresh.receive() }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    if (auth.currentUser?.id == userId && generation == session) {
                        onChange(
                            emptySnapshot.copy(phase = NativeCloudPhase.ERROR, failure = cloudFailure(error)),
                        )
                    }
                }
            }
        return {
            if (generation == session) generation++
            job.cancel()
            if (wake === refresh) wake = null
        }
    }

    override fun write(
        operation: String,
        userId: String,
        coupleId: String?,
        documentId: String,
        fields: Map<String, Any?>,
        complete: (NativeCloudFailure?) -> Unit,
    ) {
        val session = generation
        scope.launch {
            val failure =
                try {
                    operations.withLock {
                        transaction(userId, session) { write(operation, userId, coupleId, documentId, fields) }
                    }
                    wake?.trySend(Unit)
                    null
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    cloudFailure(error, operation == "invite" || operation == "join")
                }
            complete(failure)
        }
    }

    private suspend fun <T> transaction(uid: String, session: Int, action: suspend Transaction.() -> T): T {
        repeat(5) { attempt ->
            check(auth.currentUser?.id == uid && generation == session)
            val token = tokenFor(uid)
            // Firebase user tokens cannot begin server read-write transactions. Use optimistic
            // preconditions, as the client SDKs do, and retry the complete read/modify/commit cycle.
            val tx = Transaction(uid, token, session)
            try {
                val result = tx.action()
                if (tx.writes.isNotEmpty()) tx.call(":commit", mapOf("writes" to tx.guardedWrites()))
                return result
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: DesktopFirestoreFailure) {
                if (failure.status !in listOf("ABORTED", "FAILED_PRECONDITION", "ALREADY_EXISTS") || attempt == 4) {
                    throw failure
                }
            }
            delay(100L shl attempt)
        }
        error("Transaction retry exhausted")
    }

    private inner class Transaction(val uid: String, val token: String, val session: Int) {
        val writes = mutableListOf<Map<String, Any>>()
        private val reads = mutableMapOf<String, DesktopDocumentRead>()
        private var readTime: String? = null

        fun guardedWrites(): List<Map<String, Any>> {
            val unwritten = reads.toMutableMap()
            val guarded =
                writes.map { write ->
                    val name =
                        (write["update"] as? Map<*, *>)?.get("name")
                            ?: (write["transform"] as? Map<*, *>)?.get("document") ?: write["delete"]
                    val read = unwritten.remove(name)
                    if (read == null) write else write + ("currentDocument" to precondition(read))
                }
            return guarded +
                unwritten.map { (name, read) ->
                    mapOf("verify" to name, "currentDocument" to precondition(read))
                }
        }

        private fun precondition(read: DesktopDocumentRead): Map<String, Any> =
            if (read.fields == null) {
                mapOf(
                    "exists" to false,
                )
            } else {
                mapOf("updateTime" to requireNotNull(read.updateTime))
            }

        suspend fun <T> io(action: () -> T): T {
            check(auth.currentUser?.id == uid && generation == session)
            val result = withContext(Dispatchers.IO) { action() }
            check(auth.currentUser?.id == uid && generation == session)
            return result
        }

        suspend fun call(suffix: String, body: Any) = io { api.request(token, suffix, body) }

        suspend fun get(path: String): Map<String, Any?>? {
            val name = "${api.root}/$path"
            val read =
                reads[name] ?: io { api.get(token, path, readTime) }.also {
                    reads[name] = it
                    if (readTime == null) readTime = it.readTime
                }
            return read.fields
        }

        suspend fun snapshot(uid: String): NativeCloudSnapshot {
            val profile = get("users/$uid")
            if (profile == null) {
                writes += api.update("users/$uid", mapOf("name" to "", "coupleId" to null), exists = false)
                return emptySnapshot.copy(phase = NativeCloudPhase.UNPAIRED)
            }
            val name = profile["name"] as String
            val pair =
                profile["coupleId"] as String? ?: return emptySnapshot.copy(
                    phase = NativeCloudPhase.UNPAIRED,
                    displayName = name,
                )
            val couple = requireNotNull(get("couples/${segment(pair)}"))
            val members = couple["memberIds"] as List<*>
            check(uid in members && members.size in 1..2 && members.distinct().size == members.size)
            val base = emptySnapshot.copy(displayName = name, coupleId = pair)
            if (members.size == 1) {
                return base.copy(
                    phase = NativeCloudPhase.UNPAIRED,
                    inviteCode = couple["inviteId"] as String,
                )
            }
            val partner = members.single { it != uid } as String
            val partnerName = requireNotNull(get("users/${segment(partner)}"))["name"] as String
            check(partnerName.isNotBlank())
            return base.copy(
                phase = NativeCloudPhase.READY,
                partnerId = partner,
                partnerName = partnerName,
                wishes = io { api.wishes(token, pair, requireNotNull(readTime)) },
            )
        }

        suspend fun write(operation: String, uid: String, pair: String?, document: String, fields: Map<String, Any?>) {
            val profilePath = "users/${segment(uid)}"
            when (operation) {
                "rename" -> {
                    writes.add(api.update(profilePath, mapOf("name" to fields["name"]), listOf("name"), exists = true))
                }

                "invite" -> {
                    val pairId = segment(fields["pairId"] as String)
                    val profile = requireNotNull(get(profilePath))
                    check(profile["coupleId"] == null || profile["coupleId"] == pairId)
                    val existing = get("couples/$pairId")
                    if (existing == null) {
                        writes +=
                            api.update(
                                "couples/$pairId",
                                mapOf("memberIds" to listOf(uid), "inviteId" to document),
                                timestamps = listOf("createdAt"),
                                exists = false,
                            )
                    } else {
                        check(existing["memberIds"] == listOf(uid))
                        writes +=
                            api.update(
                                "couples/$pairId",
                                mapOf("inviteId" to document),
                                listOf("inviteId"),
                                exists = true,
                            )
                    }
                    writes +=
                        api.update(
                            profilePath,
                            mapOf("name" to fields["name"], "coupleId" to pairId),
                            listOf("name", "coupleId"),
                            exists = true,
                        )
                    writes +=
                        api.update(
                            "invites/${segment(document)}",
                            mapOf(
                                "ownerId" to uid,
                                "coupleId" to pairId,
                                "expiresAt" to Instant.now().plusSeconds(86_400),
                                "usedBy" to null,
                            ),
                            timestamps = listOf("createdAt"),
                            exists = false,
                        )
                }

                "join" -> {
                    val invitePath = "invites/${segment(document)}"
                    val invite = requireNotNull(get(invitePath))
                    val profile = requireNotNull(get(profilePath))
                    check(profile["coupleId"] == null && invite["ownerId"] != uid && invite["usedBy"] == null)
                    check((invite["expiresAt"] as Instant).isAfter(Instant.now()))
                    val pairId = segment(invite["coupleId"] as String)
                    // A non-member cannot read the couple. The rules validate this atomic union.
                    writes +=
                        mapOf(
                            "transform" to
                                mapOf(
                                    "document" to "${api.root}/couples/$pairId",
                                    "fieldTransforms" to
                                        listOf(
                                            mapOf(
                                                "fieldPath" to "memberIds",
                                                "appendMissingElements" to
                                                    mapOf(
                                                        "values" to listOf(firestoreValue(uid)),
                                                    ),
                                            ),
                                        ),
                                ),
                            "currentDocument" to mapOf("exists" to true),
                        )
                    writes +=
                        api.update(
                            profilePath,
                            mapOf("name" to fields["name"], "coupleId" to pairId),
                            listOf("name", "coupleId"),
                            exists = true,
                        )
                    writes += api.update(invitePath, mapOf("usedBy" to uid), listOf("usedBy"), exists = true)
                }

                else -> {
                    writeWish(operation, uid, requireNotNull(pair), document, fields)
                }
            }
        }

        private suspend fun writeWish(
            operation: String,
            uid: String,
            pair: String,
            document: String,
            fields: Map<String, Any?>,
        ) {
            val path = "couples/${segment(pair)}/wishes/${segment(document)}"
            when (operation) {
                "create" -> {
                    // A retry after a lost commit response must not overwrite an existing wish.
                    if (get(path) != null) return
                    writes +=
                        api.update(
                            path,
                            mapOf(
                                "authorId" to uid,
                                "createdOn" to fields["createdOn"],
                                "content" to fields["content"],
                                "completion" to null,
                            ),
                            timestamps = listOf("createdAt", "updatedAt"),
                            exists = false,
                        )
                }

                "delete" -> {
                    writes.add(mapOf("delete" to "${api.root}/$path"))
                }

                "edit" -> {
                    writes.add(
                        api.update(
                            path,
                            mapOf("content" to fields["content"]),
                            listOf("content"),
                            listOf("updatedAt"),
                            true,
                        ),
                    )
                }

                "complete" -> {
                    if (requireNotNull(get(path))["completion"] != null) return
                    writes +=
                        api.update(
                            path,
                            mapOf("completion" to mapOf("date" to fields["date"], "authorId" to uid, "memory" to "")),
                            listOf("completion"),
                            listOf("updatedAt"),
                            true,
                        )
                }

                "restore" -> {
                    writes.add(
                        api.update(path, mapOf("completion" to null), listOf("completion"), listOf("updatedAt"), true),
                    )
                }

                "memory" -> {
                    writes.add(
                        api.update(
                            path,
                            mapOf("completion" to mapOf("memory" to fields["memory"])),
                            listOf("completion.memory"),
                            listOf("updatedAt"),
                            true,
                        ),
                    )
                }

                else -> {
                    error("Unknown cloud operation")
                }
            }
        }
    }

    fun close() = scope.cancel()
}

private fun segment(value: String): String =
    value.also {
        require(it.isNotBlank() && it.none { char -> char in "/?#%" })
    }

private val emptySnapshot =
    NativeCloudSnapshot(NativeCloudPhase.LOADING, "", null, null, null, null, emptyList(), false, false, null)

internal fun cloudFailure(error: Exception, invitation: Boolean = false): NativeCloudFailure =
    when {
        error is IOException -> {
            NativeCloudFailure.NETWORK
        }

        error is DesktopAuthFailure -> {
            if (error.error in listOf(AuthError.NETWORK, AuthError.TOO_MANY_REQUESTS)) {
                NativeCloudFailure.NETWORK
            } else {
                NativeCloudFailure.ACCESS
            }
        }

        error is DesktopFirestoreFailure -> {
            when (error.status) {
                "PERMISSION_DENIED", "UNAUTHENTICATED" -> {
                    if (invitation) NativeCloudFailure.INVITE else NativeCloudFailure.ACCESS
                }

                "UNAVAILABLE", "DEADLINE_EXCEEDED", "ABORTED", "RESOURCE_EXHAUSTED" -> {
                    NativeCloudFailure.NETWORK
                }

                else -> {
                    if (invitation) NativeCloudFailure.INVITE else NativeCloudFailure.DATA
                }
            }
        }

        invitation -> {
            NativeCloudFailure.INVITE
        }

        error is IllegalArgumentException || error is IllegalStateException -> {
            NativeCloudFailure.DATA
        }

        else -> {
            NativeCloudFailure.UNKNOWN
        }
    }
