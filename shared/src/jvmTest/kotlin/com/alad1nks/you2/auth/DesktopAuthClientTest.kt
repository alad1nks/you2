package com.alad1nks.you2.auth

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopAuthClientTest {
    @Test
    fun registrationPersistsOnlyRefreshTokenAndSignOutClearsIt() =
        scenario { client, api, store ->
            assertNull(complete { client.register("person@example.com", "password", it) })
            assertEquals("signUp", api.lastMethod)
            assertEquals("user", client.currentUser?.id)
            assertEquals("refresh", store.token)
            assertNull(complete(client::signOut))
            assertNull(client.currentUser)
            assertNull(store.token)
        }

    @Test
    fun restoresAndRotatesSavedSession() =
        scenario(MemoryStore("old")) { client, api, store ->
            assertEquals("old", api.refreshed)
            assertEquals("rotated", store.token)
            assertEquals("person@example.com", client.currentUser?.email)
            assertFalse(client.restoringSession)
        }

    @Test
    fun invalidSessionIsClearedButOfflineStartupPreservesIt() {
        val invalid = FakeApi().apply { failure = DesktopAuthFailure(AuthError.CREDENTIALS) }
        scenario(MemoryStore("old"), invalid) { client, _, store ->
            assertNull(client.currentUser)
            assertNull(store.token)
        }
        val offline = FakeApi().apply { failure = IOException() }
        scenario(MemoryStore("old"), offline) { client, _, store ->
            assertNull(client.currentUser)
            assertEquals("old", store.token)
            assertFalse(client.restoringSession)
        }
    }

    @Test
    fun errorsResetAndGoogleExchangeUseSharedContract() =
        scenario { client, api, _ ->
            api.failure = IOException()
            assertEquals(AuthError.NETWORK, complete { client.signIn("email", "password", it) })
            api.failure = DesktopAuthFailure(AuthError.CREDENTIALS)
            assertEquals(AuthError.CREDENTIALS, complete { client.signIn("email", "password", it) })
            api.failure = null
            assertNull(complete { client.resetPassword("email", it) })
            assertEquals("PASSWORD_RESET", api.values["requestType"])
            assertNull(client.currentUser)
            assertNull(complete { client.signInWithGoogle("a&b", it) })
            assertEquals("id_token=a%26b&providerId=google.com", api.values["postBody"])
        }

    @Test
    fun unsubscribeAndFailedCredentialDeletionAreSafe() =
        scenario { client, _, store ->
            var events = 0
            val unsubscribe = client.observeUser { events++ }
            unsubscribe()
            unsubscribe()
            assertNull(complete { client.signIn("email", "password", it) })
            assertEquals(1, events)
            store.failWrites = true
            assertEquals(AuthError.UNKNOWN, complete(client::signOut))
            assertEquals("user", client.currentUser?.id)
            assertEquals("refresh", store.token)
        }

    @Test
    fun parsesCallbacksAndFirebaseErrors() {
        assertEquals("a+b", parseOAuthCallback("state=x&code=a%2Bb")["code"])
        assertTrue(parseOAuthCallback("code=%invalid").isEmpty())
        assertEquals(AuthError.WEAK_PASSWORD, desktopAuthError("WEAK_PASSWORD : policy"))
        assertEquals(AuthError.CREDENTIALS, desktopAuthError("INVALID_LOGIN_CREDENTIALS"))
        assertEquals(AuthError.UNKNOWN, desktopAuthError("OPERATION_NOT_ALLOWED"))
    }

    private fun scenario(
        store: MemoryStore = MemoryStore(),
        api: FakeApi = FakeApi(),
        block: suspend (DesktopAuthClient, FakeApi, MemoryStore) -> Unit,
    ) = runBlocking {
        withTimeout(10_000) {
            withContext(Dispatchers.Main) {
                val client = DesktopAuthClient(api, store)
                try {
                    val restored = CompletableDeferred<Unit>()
                    val unsubscribe = client.observeUser { if (!client.restoringSession) restored.complete(Unit) }
                    restored.await()
                    unsubscribe()
                    block(client, api, store)
                } finally {
                    client.close()
                }
            }
        }
    }

    private suspend fun complete(start: ((AuthError?) -> Unit) -> Unit): AuthError? {
        val result = CompletableDeferred<AuthError?>()
        start { result.complete(it) }
        return result.await()
    }

    private class MemoryStore(var token: String? = null) : DesktopSessionStore {
        var failWrites = false

        override fun read() = token

        override fun write(refreshToken: String) {
            check(!failWrites)
            token = refreshToken
        }

        override fun clear() {
            check(!failWrites)
            token = null
        }
    }

    private class FakeApi : DesktopAuthApi() {
        var failure: Exception? = null
        var lastMethod: String? = null
        var values: Map<String, Any> = emptyMap()
        var refreshed: String? = null

        override fun request(method: String, values: Map<String, Any>): JsonObject {
            failure?.let { throw it }
            lastMethod = method
            this.values = values
            return JsonParser.parseString(
                if (method == "lookup") {
                    """{"users":[{"localId":"user","email":"person@example.com"}]}"""
                } else {
                    """{"localId":"user","email":"person@example.com","refreshToken":"refresh"}"""
                },
            ).asJsonObject
        }

        override fun refresh(token: String): JsonObject {
            failure?.let { throw it }
            refreshed = token
            return JsonParser.parseString("""{"refresh_token":"rotated","id_token":"id"}""").asJsonObject
        }
    }
}
