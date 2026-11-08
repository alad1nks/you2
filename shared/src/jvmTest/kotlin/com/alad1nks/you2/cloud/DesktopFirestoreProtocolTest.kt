package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.AuthUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The emulator accepts server transactions that production rejects for Firebase user tokens. */
class DesktopFirestoreProtocolTest {
    @Test
    fun loadsProfileAndRetriesConflictsWithoutServerTransactions() =
        runBlocking {
            val requests = CopyOnWriteArrayList<Pair<String, JsonObject>>()
            var wishReads = 0
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            server.createContext("/") { exchange ->
                val method = exchange.requestURI.path.substringAfterLast(':')
                val request = JsonParser.parseString(exchange.requestBody.bufferedReader().readText()).asJsonObject
                requests += method to request
                val (status, response) =
                    when (method) {
                        "batchGet" -> {
                            val name = request.getAsJsonArray("documents").single().asString
                            val fields =
                                if (name.endsWith("users/alice")) {
                                    mapOf("name" to "Alice", "coupleId" to null)
                                } else {
                                    wishReads++
                                    mapOf("completion" to if (wishReads == 1) null else mapOf("authorId" to "bob"))
                                }
                            200 to
                                listOf(
                                    mapOf(
                                        "found" to
                                            mapOf(
                                                "name" to name,
                                                "fields" to fields.mapValues { firestoreValue(it.value) },
                                                "updateTime" to "2026-11-08T00:00:00Z",
                                            ),
                                        "readTime" to "2026-11-08T00:00:01Z",
                                    ),
                                )
                        }

                        "commit" -> {
                            400 to mapOf("error" to mapOf("status" to "FAILED_PRECONDITION"))
                        }

                        else -> {
                            403 to mapOf("error" to mapOf("status" to "PERMISSION_DENIED"))
                        }
                    }
                val bytes = Gson().toJson(response).toByteArray()
                exchange.sendResponseHeaders(status, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
            try {
                withContext(Dispatchers.Main) {
                    withTimeout(10_000) {
                        val auth =
                            object : AuthClient {
                                override val currentUser = AuthUser("alice", "alice@example.com")

                                override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit = {}

                                override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) =
                                    error(
                                        "Unused",
                                    )

                                override fun register(email: String, password: String, complete: (AuthError?) -> Unit) =
                                    error(
                                        "Unused",
                                    )

                                override fun resetPassword(email: String, complete: (AuthError?) -> Unit) =
                                    error(
                                        "Unused",
                                    )
                            }
                        val api = DesktopFirestoreApi("demo-you2", "http://127.0.0.1:${server.address.port}/v1")
                        val provider = DesktopCloudProvider(auth, { "user-token" }, api)
                        try {
                            val loaded = CompletableDeferred<NativeCloudSnapshot>()
                            val stop = provider.observe("alice") { loaded.complete(it) }
                            assertEquals(NativeCloudPhase.UNPAIRED, loaded.await().phase)
                            stop()
                            val complete = CompletableDeferred<NativeCloudFailure?>()
                            provider.write("complete", "alice", "pair", "wish", mapOf("date" to "2026-11-08")) {
                                complete.complete(it)
                            }
                            assertNull(complete.await())
                            assertEquals(2, wishReads)
                            val commit = requests.single { it.first == "commit" }.second
                            val write = commit.getAsJsonArray("writes").single().asJsonObject
                            assertEquals(
                                "2026-11-08T00:00:00Z",
                                write.getAsJsonObject("currentDocument").get("updateTime").asString,
                            )
                            assertTrue(requests.all { it.first in setOf("batchGet", "commit") })
                            assertFalse(requests.any { it.second.has("transaction") })
                        } finally {
                            provider.close()
                        }
                    }
                }
            } finally {
                server.stop(0)
            }
        }
}
