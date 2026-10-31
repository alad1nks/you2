package com.alad1nks.you2.core.data.repository

import android.net.Uri
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

/** Serializes uploads and durable orphan cleanup. Never creates public download URLs. */
internal class WishImageUploads {
    val executor = Executors.newSingleThreadExecutor()
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth = FirebaseAuth.getInstance()
    private val preferences = FirebaseApp.getInstance().applicationContext.getSharedPreferences("image-cleanup", 0)

    fun execute(operation: () -> Unit): Task<Void> {
        val result = TaskCompletionSource<Void>()
        executor.execute {
            try {
                operation()
                result.setResult(null)
            } catch (error: Exception) {
                result.setException((error.cause as? Exception) ?: error)
            }
        }
        return result.task
    }

    fun checkUser(uid: String) {
        check(auth.currentUser?.uid == uid)
    }

    private fun queue(uid: String): Set<String> = preferences.getStringSet(uid, emptySet()).orEmpty().toSet()

    fun track(uid: String, paths: Collection<String>) {
        if (paths.isNotEmpty()) check(preferences.edit().putStringSet(uid, queue(uid) + paths).commit())
    }

    fun paths(draft: WishDraft): List<String> =
        (listOfNotNull(draft.cover) + draft.attachments).mapNotNull {
            it.source?.takeIf {
                path ->
                path.startsWith("couples/")
            }
        }

    fun upload(
        uid: String,
        document: DocumentReference,
        draft: WishDraft,
        uploaded: MutableMap<String, WishImage>,
    ): WishDraft {
        fun resolve(image: WishImage): WishImage {
            if (!image.isLocal) return image
            uploaded[image.key]?.let { return it }
            checkUser(uid)
            val path = "${document.path}/${UUID.randomUUID()}.jpg"
            track(uid, listOf(path))
            val file = File(requireNotNull(Uri.parse(image.source).path))
            require(file.length() in 1..5_242_880)
            // Describe the re-encoded JPEG explicitly rather than relying on server defaults.
            val metadata =
                StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .setContentEncoding("identity")
                    .setContentDisposition("inline")
                    .build()
            Tasks.await(storage.reference.child(path).putFile(Uri.fromFile(file), metadata))
            checkUser(uid)
            return WishImage(source = path).also { uploaded[image.key] = it }
        }
        return draft.copy(cover = draft.cover?.let(::resolve), attachments = draft.attachments.map(::resolve))
    }

    /** Called on the serial worker after saves, or on reconnect. Failed deletions stay queued. */
    fun cleanup(uid: String) {
        for (path in queue(uid)) {
            if (auth.currentUser?.uid != uid) return
            try {
                val document = FirebaseFirestore.getInstance().document(path.substringBeforeLast('/'))
                val snapshot = Tasks.await(document.get(Source.SERVER))
                val data = snapshot.data
                val referenced = data != null && path in paths(WishDocument.decode(snapshot.id, data).content)
                if (!referenced) {
                    checkUser(uid)
                    try {
                        Tasks.await(storage.reference.child(path).delete())
                    } catch (error: Exception) {
                        val code = (error.cause as? StorageException)?.errorCode
                        if (code != StorageException.ERROR_OBJECT_NOT_FOUND) throw error
                    }
                }
                check(preferences.edit().putStringSet(uid, queue(uid) - path).commit())
            } catch (_: Exception) {
                // Server state must be known before deleting; retry next save/session.
            }
        }
    }
}
