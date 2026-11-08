package com.alad1nks.you2.cloud

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DesktopStorageTest {
    @Test
    fun cleanupPersistsPerAccountAcrossInstances() {
        val directory = Files.createTempDirectory("you2-cleanup")
        val queue = DesktopImageCleanup(directory)
        val path = "couples/p/wishes/w/i.jpg"
        queue.track("alice", setOf(path))
        val restored = DesktopImageCleanup(directory)
        assertEquals(setOf(path), restored.paths("alice"))
        assertTrue(restored.paths("bob").isEmpty())
        restored.forget("alice", path)
        assertTrue(queue.paths("alice").isEmpty())
    }

    @Test
    fun boundsDownloadsAndDoesNotLeakServerErrors() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { request ->
            assertEquals("Firebase user-token", request.requestHeaders.getFirst("Authorization"))
            val status = if (request.requestURI.path.endsWith("denied")) 403 else 200
            val bytes = if (status == 403) "sensitive-server-message".toByteArray() else ByteArray(MAX_IMAGE_BYTES + 1)
            request.sendResponseHeaders(status, bytes.size.toLong())
            runCatching { request.responseBody.use { it.write(bytes) } }
            request.close()
        }
        server.start()
        try {
            val api = DesktopStorageApi("bucket", "http://127.0.0.1:${server.address.port}/v0")
            val failure = assertFailsWith<DesktopStorageFailure> { api.download("user-token", "denied") }
            assertEquals(403, failure.code)
            assertEquals(null, failure.message)
            assertEquals(NativeCloudFailure.STORAGE_ACCESS, cloudFailure(failure))
            assertFailsWith<java.io.IOException> { api.download("user-token", "huge") }
        } finally {
            server.stop(0)
        }
    }
}
