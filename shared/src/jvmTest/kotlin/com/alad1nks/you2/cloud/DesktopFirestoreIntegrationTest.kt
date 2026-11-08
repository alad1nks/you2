package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.AuthUser
import com.alad1nks.you2.core.data.repository.WishDocument
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDraft
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import java.util.Base64
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Uses only demo-you2 and user JWTs accepted by the emulator, never admin bypass credentials. */
class DesktopFirestoreIntegrationTest {
    @Test
    fun pairingAndWishLifecycleObeyTheDeployedRules() {
        val host = System.getenv("FIRESTORE_EMULATOR_HOST")
        assumeTrue("Run through firebase emulators:exec", host != null)
        runBlocking {
            withContext(Dispatchers.Main) {
                withTimeout(90_000) {
                    val alice = Client("alice", host!!)
                    val bob = Client("bob", host)
                    val outsider = Client("outsider", host)
                    try {
                        alice.await { it.phase == NativeCloudPhase.UNPAIRED }
                        bob.await { it.phase == NativeCloudPhase.UNPAIRED }
                        outsider.await { it.phase == NativeCloudPhase.UNPAIRED }
                        val pair = alice.provider.newId()
                        val oldCode = alice.provider.newId()
                        val code = alice.provider.newId()
                        assertNull(
                            alice.write("invite", id = oldCode, fields = mapOf("pairId" to pair, "name" to "Alice")),
                        )
                        assertNull(
                            alice.write("invite", id = code, fields = mapOf("pairId" to pair, "name" to "Alice")),
                        )
                        assertEquals(
                            NativeCloudFailure.INVITE,
                            bob.write("join", id = oldCode, fields = mapOf("name" to "Bob")),
                        )
                        assertNull(bob.write("join", id = code, fields = mapOf("name" to "Bob")))
                        alice.await { it.phase == NativeCloudPhase.READY && it.partnerName == "Bob" }
                        bob.await { it.phase == NativeCloudPhase.READY && it.partnerName == "Alice" }
                        assertEquals(
                            NativeCloudFailure.INVITE,
                            outsider.write("join", id = code, fields = mapOf("name" to "Eve")),
                        )

                        val id = alice.provider.newId()
                        val content =
                            WishDocument.content(
                                WishDraft(title = "A walk", category = WishCategory.ADVENTURES),
                            )
                        val create = mapOf("content" to content, "createdOn" to "2026-11-08")
                        assertNull(alice.write("create", pair, id, create))
                        val created = bob.await { it.wishes.size == 1 }.wishes.single()
                        assertEquals("A walk", WishDocument.decode(created.id, created.fields).content.title)
                        assertNull(
                            bob.write("edit", pair, id, mapOf("content" to content + ("title" to "A long walk"))),
                        )
                        // Retrying create after a lost response must not undo another device's edit.
                        assertNull(alice.write("create", pair, id, create))
                        alice.await {
                            it.wishes.singleOrNull()?.fields?.get(
                                "content",
                            ) == content + ("title" to "A long walk")
                        }
                        assertEquals(NativeCloudFailure.ACCESS, outsider.write("delete", pair, id))
                        assertNull(bob.write("complete", pair, id, mapOf("date" to "2026-11-08")))
                        assertNull(alice.write("complete", pair, id, mapOf("date" to "2026-11-09")))
                        assertNull(alice.write("memory", pair, id, mapOf("memory" to "Lovely day")))
                        val completed =
                            bob.await {
                                (
                                    it.wishes.singleOrNull()?.fields?.get(
                                        "completion",
                                    ) as? Map<*, *>
                                )?.get("memory") == "Lovely day"
                            }.wishes.single().fields["completion"] as Map<*, *>
                        assertEquals(bob.uid, completed["authorId"])
                        assertEquals("2026-11-08", completed["date"])
                        assertNull(alice.write("restore", pair, id))
                        bob.await { it.wishes.size == 1 && it.wishes.single().fields["completion"] == null }
                        assertNull(alice.write("rename", fields = mapOf("name" to "Alicia")))
                        bob.await { it.partnerName == "Alicia" }
                        assertNull(bob.write("delete", pair, id))
                        alice.await { it.wishes.isEmpty() }
                        alice.stop()
                        val events = alice.events
                        assertNull(bob.write("rename", fields = mapOf("name" to "Robert")))
                        kotlinx.coroutines.delay(300)
                        assertEquals(events, alice.events)
                    } finally {
                        alice.close()
                        bob.close()
                        outsider.close()
                    }
                }
            }
        }
    }

    private class Client(prefix: String, host: String) {
        val uid = "$prefix-${UUID.randomUUID()}"
        val auth = TestAuth(uid)
        val provider =
            DesktopCloudProvider(auth, { jwt(uid) }, DesktopFirestoreApi("demo-you2", "http://$host/v1"), 100)
        val snapshots = Channel<NativeCloudSnapshot>(Channel.UNLIMITED)
        var events = 0
        val stop =
            provider.observe(uid) {
                events++
                snapshots.trySend(it)
            }

        suspend fun await(predicate: (NativeCloudSnapshot) -> Boolean): NativeCloudSnapshot {
            while (true) {
                val snapshot = snapshots.receive()
                assertTrue(snapshot.phase != NativeCloudPhase.ERROR, "Cloud failed: ${snapshot.failure}")
                if (predicate(snapshot)) return snapshot
            }
        }

        suspend fun write(
            operation: String,
            pair: String? = null,
            id: String = "",
            fields: Map<String, Any?> = emptyMap(),
        ): NativeCloudFailure? {
            val result = CompletableDeferred<NativeCloudFailure?>()
            provider.write(operation, uid, pair, id, fields) { result.complete(it) }
            return result.await()
        }

        fun close() {
            stop()
            provider.close()
        }
    }

    private class TestAuth(uid: String) : AuthClient {
        override var currentUser: AuthUser? = AuthUser(uid, "$uid@example.com")

        override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit = { }

        override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) = error("Unused")

        override fun register(email: String, password: String, complete: (AuthError?) -> Unit) = error("Unused")

        override fun resetPassword(email: String, complete: (AuthError?) -> Unit) = error("Unused")
    }

    companion object {
        private fun jwt(uid: String): String {
            fun encode(
                value: Any,
            ) = Base64.getUrlEncoder().withoutPadding().encodeToString(Gson().toJson(value).toByteArray())
            val now = System.currentTimeMillis() / 1000
            return encode(mapOf("alg" to "none", "typ" to "JWT")) + "." +
                encode(
                    mapOf(
                        "sub" to uid,
                        "user_id" to uid,
                        "aud" to "demo-you2",
                        "iss" to "https://securetoken.google.com/demo-you2",
                        "iat" to now,
                        "exp" to now + 3600,
                        "auth_time" to now,
                        "firebase" to mapOf("sign_in_provider" to "custom", "identities" to emptyMap<String, Any>()),
                    ),
                ) + "."
        }
    }
}
