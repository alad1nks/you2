package com.alad1nks.you2.cloud

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/** Persist before uploading/deleting; failed cleanup survives process restarts, partitioned by account. */
internal class DesktopImageCleanup(
    private val directory: Path = Path.of(System.getProperty("user.home"), ".you2", "image-cleanup"),
) {
    private val gson = Gson()

    fun paths(uid: String): Set<String> {
        val file = file(uid)
        if (!Files.exists(file)) return emptySet()
        return gson.fromJson(Files.readString(file), Array<String>::class.java).toSet()
    }

    fun track(uid: String, paths: Set<String>) {
        if (paths.isNotEmpty()) save(uid, paths(uid) + paths)
    }

    fun forget(uid: String, path: String) = save(uid, paths(uid) - path)

    private fun save(uid: String, paths: Set<String>) {
        Files.createDirectories(directory)
        val temporary = Files.createTempFile(directory, "cleanup-", ".tmp")
        try {
            Files.writeString(temporary, gson.toJson(paths))
            Files.move(temporary, file(uid), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun file(uid: String): Path {
        val key = MessageDigest.getInstance("SHA-256").digest(uid.toByteArray()).joinToString("") { "%02x".format(it) }
        return directory.resolve("$key.json")
    }
}

internal fun desktopImagePaths(content: Map<*, *>): Set<String> =
    (
        listOfNotNull(
            content["cover"] as? String,
        ) + (content["attachments"] as? List<*>).orEmpty().filterIsInstance<String>()
    )
        .filter { it.startsWith("couples/") }.toSet()
