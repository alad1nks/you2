package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise

/** Called under the provider's write mutex; cleanup never races an in-flight upload. */
internal class WebWishImages(private val auth: AuthClient, private val db: dynamic) {
    private val storage by lazy { getStorage(db.app) }
    private val scope = MainScope()
    private val localStorage: dynamic get() = js("globalThis.localStorage")

    private fun checkUser(uid: String) = check(auth.currentUser?.id == uid)

    private fun queue(uid: String): Set<String> {
        val stored = localStorage.getItem("image-cleanup-$uid") as String? ?: return emptySet()
        return (JSON.parse<Array<String>>(stored)).toSet()
    }

    private fun track(uid: String, paths: Set<String>) {
        if (paths.isNotEmpty()) {
            localStorage.setItem(
                "image-cleanup-$uid",
                JSON.stringify((queue(uid) + paths).toTypedArray()),
            )
        }
    }

    private suspend fun transaction(uid: String, body: suspend (WebTransaction) -> Unit) {
        runTransaction(db) { tx ->
            scope.promise {
                checkUser(uid)
                body(tx)
                checkUser(uid)
            }
        }.await()
    }

    suspend fun write(operation: String, uid: String, pair: String, id: String, fields: Map<String, Any?>) {
        checkUser(uid)
        val path = "couples/$pair/wishes/$id"
        val document = doc(db, path)
        if (operation == "delete") {
            transaction(uid) { tx ->
                val old = tx.get(document).await()
                track(uid, imagePaths(if (old.exists() as Boolean) webFields(old.data().content) else emptyMap()))
                tx.delete(document)
            }
            cleanup(uid)
            return
        }
        @Suppress("UNCHECKED_CAST")
        val content = fields["content"] as Map<String, Any?>
        val attachments = content["attachments"] as List<*>
        require(attachments.size <= 12)
        val sources = listOfNotNull(content["cover"] as String?) + attachments.map { it as String }
        val local = sources.filter { it.startsWith("data:image/jpeg;base64,") }.distinct()
        var createSeed: Map<String, Any?>? = null
        if (operation == "create") {
            val seed =
                content +
                    mapOf(
                        "cover" to (content["cover"] as String?)?.takeUnless { it in local },
                        "attachments" to attachments.filterNot { it in local },
                    )
            createSeed = seed
            var needsImages = true
            transaction(uid) { tx ->
                val existing = tx.get(document).await()
                needsImages = !(existing.exists() as Boolean) ||
                    (webFields(existing.data().content) == seed && existing.data().completion == null)
                if (!(existing.exists() as Boolean)) {
                    tx.set(
                        document,
                        webObject(
                            mapOf(
                                "authorId" to uid,
                                "createdOn" to fields["createdOn"],
                                "createdAt" to serverTimestamp(),
                                "updatedAt" to serverTimestamp(),
                                "content" to seed,
                                "completion" to null,
                            ),
                        ),
                    )
                }
            }
            if (local.isEmpty() || !needsImages) return
        }
        val uploaded = mutableMapOf<String, String>()
        try {
            for (source in local) {
                checkUser(uid)
                require(jpegBytes(source).size in 1..5_242_880)
                val target = "$path/${js("globalThis.crypto.randomUUID()") as String}.jpg"
                track(uid, setOf(target))
                uploadString(
                    ref(storage, target),
                    source,
                    "data_url",
                    webObject(
                        mapOf(
                            "contentType" to "image/jpeg",
                            "contentEncoding" to "identity",
                            "contentDisposition" to "inline",
                        ),
                    ),
                ).await()
                checkUser(uid)
                uploaded[source] = target
            }
            val resolved =
                content +
                    mapOf(
                        "cover" to (content["cover"] as String?)?.let { uploaded[it] ?: it },
                        "attachments" to attachments.map { uploaded[it] ?: it },
                    )
            transaction(uid) { tx ->
                val old = tx.get(document).await()
                check(old.exists() as Boolean)
                val oldContent = webFields(old.data().content)
                // A retried create must not overwrite an acknowledged save or a partner's edit.
                if (createSeed != null && (oldContent != createSeed || old.data().completion != null)) {
                    return@transaction
                }
                val previous = imagePaths(oldContent)
                require(imagePaths(resolved).all { it in previous || it in uploaded.values })
                track(uid, previous)
                tx.update(document, webObject(mapOf("content" to resolved, "updatedAt" to serverTimestamp())))
            }
        } finally {
            cleanup(uid)
        }
    }

    suspend fun cleanup(uid: String) {
        // A failed read must never be treated as evidence that a file is unreferenced.
        val pending =
            try {
                queue(uid)
            } catch (_: Throwable) {
                return
            }
        for (path in pending) {
            try {
                checkUser(uid)
                val snapshot = getDocFromServer(doc(db, path.substringBeforeLast('/'))).await()
                checkUser(uid)
                val referenced = snapshot.exists() as Boolean && path in imagePaths(webFields(snapshot.data().content))
                if (!referenced) {
                    try {
                        deleteObject(ref(storage, path)).await()
                    } catch (error: Throwable) {
                        if (error.asDynamic().code != "storage/object-not-found") throw error
                    }
                }
                localStorage.setItem("image-cleanup-$uid", JSON.stringify((queue(uid) - path).toTypedArray()))
            } catch (_: Throwable) {
                // Retain failures for the next save or session.
            }
        }
    }
}

internal fun imagePaths(content: Map<String, Any?>): Set<String> {
    val cover = listOfNotNull(content["cover"] as? String)
    val attachments = (content["attachments"] as? List<*>).orEmpty().filterIsInstance<String>()
    return (cover + attachments).filter { it.startsWith("couples/") }.toSet()
}

internal fun jpegBytes(source: String): ByteArray {
    require(source.startsWith("data:image/jpeg;base64,"))
    val encoded = source.substringAfter(',')
    val binary = js("globalThis.atob(encoded)") as String
    return ByteArray(binary.length) { binary[it].code.toByte() }
}
