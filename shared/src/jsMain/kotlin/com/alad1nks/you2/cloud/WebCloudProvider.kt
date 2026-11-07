package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.firebaseApp
import kotlin.js.Date
import kotlin.js.Promise

/** Uses the same schema and atomic pairing protocol as the mobile clients. */
internal class WebCloudProvider(
    private val auth: AuthClient,
    private val db: dynamic = getFirestore(firebaseApp),
) : NativeCloudProvider {
    override fun newId(): String = (js("globalThis.crypto.randomUUID()") as String).replace("-", "")

    override fun observe(userId: String, onChange: (NativeCloudSnapshot) -> Unit): () -> Unit {
        val session = Session(userId, onChange)
        session.start()
        return { session.close() }
    }

    private inner class Session(val uid: String, val changed: (NativeCloudSnapshot) -> Unit) {
        var active = true
        var pairGeneration = 0
        var profileListener: (() -> Unit)? = null
        val pairListeners = mutableListOf<() -> Unit>()
        var creatingProfile = false
        var snapshot =
            NativeCloudSnapshot(NativeCloudPhase.LOADING, "", null, null, null, null, emptyList(), false, false, null)

        fun current() = active && auth.currentUser?.id == uid

        fun publish() {
            if (current()) changed(snapshot)
        }

        fun close() {
            active = false
            profileListener?.invoke()
            clearPair()
        }

        fun clearPair() {
            pairGeneration++
            pairListeners.forEach { it() }
            pairListeners.clear()
            snapshot =
                snapshot.copy(
                    coupleId = null,
                    inviteCode = null,
                    partnerId = null,
                    partnerName = null,
                    wishes = emptyList(),
                )
        }

        fun fail(failure: NativeCloudFailure) {
            if (!current()) return
            snapshot = snapshot.copy(phase = NativeCloudPhase.ERROR, wishes = emptyList(), failure = failure)
            publish()
            close()
        }

        fun listen(reference: dynamic, valid: () -> Boolean = ::current, next: (dynamic) -> Unit): () -> Unit =
            onSnapshot(reference, js("({includeMetadataChanges: true})"), { data ->
                if (valid()) {
                    try {
                        next(data)
                    } catch (_: Throwable) {
                        fail(NativeCloudFailure.DATA)
                    }
                }
            }, { error -> if (valid()) fail(webCloudFailure(error)) })

        fun start() {
            val profile = doc(db, "users/$uid")
            profileListener =
                listen(profile) { data ->
                    if (!(data.exists() as Boolean)) {
                        // A cache miss is not proof that the account has no server profile.
                        if (data.metadata.fromCache == false && !creatingProfile) {
                            creatingProfile = true
                            transaction(uid) { tx ->
                                tx.get(profile).then<Unit> { existing: dynamic ->
                                    check(current())
                                    if (!(existing.exists() as Boolean)) {
                                        tx.set(profile, webObject(mapOf("name" to "", "coupleId" to null)))
                                    }
                                    Unit
                                }
                            }.catch { error -> if (current()) fail(webCloudFailure(error)) }
                        }
                    } else {
                        val fields = data.data()
                        val name = fields.name as String
                        val pair = fields.coupleId as String?
                        snapshot = snapshot.copy(displayName = name)
                        if (pair == null) {
                            clearPair()
                            snapshot = snapshot.copy(phase = NativeCloudPhase.UNPAIRED)
                            publish()
                        } else if (pair != snapshot.coupleId) {
                            observePair(pair)
                        } else {
                            publish()
                        }
                    }
                }
        }

        fun observePair(pair: String) {
            clearPair()
            val token = pairGeneration

            fun valid() = current() && token == pairGeneration
            snapshot = snapshot.copy(coupleId = pair, phase = NativeCloudPhase.LOADING)
            publish()
            var observingContent = false
            pairListeners +=
                listen(doc(db, "couples/$pair"), ::valid) { data ->
                    check(data.exists() as Boolean)
                    val fields = data.data()
                    val members = (fields.memberIds as Array<String>).toList()
                    check(uid in members && members.distinct().size == members.size && members.size in 1..2)
                    if (members.size == 1) {
                        check(!observingContent)
                        snapshot =
                            snapshot.copy(
                                phase = NativeCloudPhase.UNPAIRED,
                                inviteCode = fields.inviteId as String,
                            )
                        publish()
                    } else if (!observingContent) {
                        observingContent = true
                        observeContent(pair, members.single { it != uid }, ::valid)
                    }
                }
        }

        fun observeContent(pair: String, partner: String, valid: () -> Boolean) {
            var partnerLoaded = false
            var wishesLoaded = false

            fun publishContent() {
                if (partnerLoaded && wishesLoaded) {
                    snapshot = snapshot.copy(phase = NativeCloudPhase.READY, inviteCode = null)
                    publish()
                }
            }
            snapshot = snapshot.copy(phase = NativeCloudPhase.LOADING, partnerId = partner, inviteCode = null)
            publish()
            pairListeners +=
                listen(doc(db, "users/$partner"), valid) { data ->
                    check(data.exists() as Boolean)
                    val name = data.data().name as String
                    check(name.isNotBlank())
                    snapshot = snapshot.copy(partnerName = name)
                    partnerLoaded = true
                    publishContent()
                }
            pairListeners +=
                listen(query(collection(db, "couples/$pair/wishes"), orderBy("createdAt", "desc")), valid) { data ->
                    val wishes =
                        (data.docs as Array<dynamic>).map {
                            NativeCloudDocument(
                                it.id as String,
                                webFields(it.data()),
                            )
                        }
                    snapshot =
                        snapshot.copy(
                            wishes = wishes,
                            fromCache = data.metadata.fromCache as Boolean,
                            pendingWrites = data.metadata.hasPendingWrites as Boolean,
                        )
                    wishesLoaded = true
                    publishContent()
                }
        }
    }

    private fun transaction(uid: String, body: (WebTransaction) -> Promise<Unit>) =
        runTransaction(db) { tx ->
            // JavaScript promises flatten the promise returned by the callback.
            Promise.resolve(Unit).then {
                check(auth.currentUser?.id == uid)
                body(tx)
            }.unsafeCast<Promise<Unit>>().then {
                // Transaction callbacks can rerun, including after an account change.
                check(auth.currentUser?.id == uid)
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
        try {
            transaction(userId) { tx ->
                val profile = doc(db, "users/$userId")
                when (operation) {
                    "invite" -> {
                        val pairId = fields["pairId"] as String
                        val pair = doc(db, "couples/$pairId")
                        Promise.all(arrayOf(tx.get(profile), tx.get(pair))).then { documents ->
                            val me = documents[0]
                            val existing = documents[1]
                            check(me.exists() as Boolean)
                            check(me.data().coupleId == null || me.data().coupleId == pairId)
                            if (existing.exists() as Boolean) {
                                check((existing.data().memberIds as Array<String>).toList() == listOf(userId))
                                tx.update(pair, webObject(mapOf("inviteId" to documentId)))
                            } else {
                                tx.set(
                                    pair,
                                    webObject(
                                        mapOf(
                                            "memberIds" to listOf(userId),
                                            "inviteId" to documentId,
                                            "createdAt" to serverTimestamp(),
                                        ),
                                    ),
                                )
                            }
                            tx.update(profile, webObject(mapOf("name" to fields["name"], "coupleId" to pairId)))
                            tx.set(
                                doc(db, "invites/$documentId"),
                                webObject(
                                    mapOf(
                                        "ownerId" to userId,
                                        "coupleId" to pairId,
                                        "createdAt" to serverTimestamp(),
                                        "expiresAt" to Timestamp.fromMillis(Date.now() + 86_400_000),
                                        "usedBy" to null,
                                    ),
                                ),
                            )
                            Unit
                        }
                    }

                    "join" -> {
                        val invite = doc(db, "invites/$documentId")
                        Promise.all(arrayOf(tx.get(invite), tx.get(profile))).then { documents ->
                            val invitation = documents[0]
                            val me = documents[1]
                            check(me.exists() as Boolean && me.data().coupleId == null)
                            check(invitation.exists() as Boolean)
                            val data = invitation.data()
                            check(
                                data.ownerId != userId && data.usedBy == null &&
                                    (data.expiresAt.toMillis() as Double) > Date.now(),
                            )
                            val pairId = data.coupleId as String
                            // The invite is the capability: a non-member cannot read the couple yet.
                            tx.update(doc(db, "couples/$pairId"), webObject(mapOf("memberIds" to arrayUnion(userId))))
                            tx.update(profile, webObject(mapOf("name" to fields["name"], "coupleId" to pairId)))
                            tx.update(invite, webObject(mapOf("usedBy" to userId)))
                            Unit
                        }
                    }

                    "rename" -> {
                        tx.update(profile, webObject(mapOf("name" to fields["name"])))
                        Promise.resolve(Unit)
                    }

                    else -> {
                        writeWish(tx, operation, userId, requireNotNull(coupleId), documentId, fields)
                    }
                }
            }.then(
                { complete(null) },
                { error -> complete(webCloudFailure(error, operation in listOf("invite", "join"))) },
            )
        } catch (error: Throwable) {
            complete(webCloudFailure(error, operation in listOf("invite", "join")))
        }
    }

    private fun writeWish(
        tx: WebTransaction,
        operation: String,
        uid: String,
        pair: String,
        id: String,
        fields: Map<String, Any?>,
    ): Promise<Unit> {
        val reference = doc(db, "couples/$pair/wishes/$id")
        return when (operation) {
            "create" -> {
                // Retrying an acknowledged write must not overwrite later edits or a memory.
                tx.get(reference).then<Unit> { existing: dynamic ->
                    if (!(existing.exists() as Boolean)) {
                        tx.set(
                            reference,
                            webObject(
                                mapOf(
                                    "authorId" to uid,
                                    "createdOn" to fields["createdOn"],
                                    "createdAt" to serverTimestamp(),
                                    "updatedAt" to serverTimestamp(),
                                    "content" to fields["content"],
                                    "completion" to null,
                                ),
                            ),
                        )
                    }
                    Unit
                }
            }

            "complete" -> {
                tx.get(reference).then<Unit> { existing: dynamic ->
                    check(existing.exists() as Boolean)
                    if (existing.data().completion == null) {
                        tx.update(
                            reference,
                            webObject(
                                mapOf(
                                    "completion" to mapOf("date" to fields["date"], "authorId" to uid, "memory" to ""),
                                    "updatedAt" to serverTimestamp(),
                                ),
                            ),
                        )
                    }
                    Unit
                }
            }

            "delete" -> {
                tx.delete(reference)
                Promise.resolve(Unit)
            }

            else -> {
                val updates =
                    when (operation) {
                        "edit" -> mapOf("content" to fields["content"])
                        "restore" -> mapOf("completion" to null)
                        "memory" -> mapOf("completion.memory" to fields["memory"])
                        else -> error("Unknown cloud operation")
                    }
                tx.update(reference, webObject(updates + ("updatedAt" to serverTimestamp())))
                Promise.resolve(Unit)
            }
        }
    }
}

/** Convert Kotlin collections only; SDK Timestamp/FieldValue objects must retain their prototypes. */
internal fun webObject(value: Any?): dynamic =
    when (value) {
        is Map<*, *> -> {
            val result = js("({})")
            value.forEach { (key, field) -> result[key as String] = webObject(field) }
            result
        }

        is List<*> -> {
            value.map { webObject(it) }.toTypedArray()
        }

        else -> {
            value
        }
    }

internal fun webFields(value: dynamic): Map<String, Any?> =
    (js("Object.keys(value)") as Array<String>).associateWith { key -> webValue(value[key]) }

private fun webValue(value: dynamic): Any? =
    when {
        value == null -> null
        js("Array.isArray(value)") as Boolean -> (value as Array<dynamic>).map { webValue(it) }
        js("Object.getPrototypeOf(value) === Object.prototype") as Boolean -> webFields(value)
        else -> value as Any
    }

internal fun webCloudFailure(error: dynamic, invitation: Boolean = false): NativeCloudFailure =
    when (error.code as? String) {
        "permission-denied", "unauthenticated" -> {
            if (invitation) NativeCloudFailure.INVITE else NativeCloudFailure.ACCESS
        }

        "unavailable", "deadline-exceeded", "aborted" -> {
            NativeCloudFailure.NETWORK
        }

        else -> {
            if (invitation) {
                NativeCloudFailure.INVITE
            } else if (error is IllegalStateException || error is IllegalArgumentException) {
                NativeCloudFailure.DATA
            } else {
                NativeCloudFailure.UNKNOWN
            }
        }
    }
