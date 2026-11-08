package com.alad1nks.you2.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

/** State and callbacks are confined to the UI dispatcher; HTTP/credential-store work runs on IO. */
internal class DesktopAuthClient(
    private val api: DesktopAuthApi = DesktopAuthApi(),
    private val store: DesktopSessionStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : AuthClient {
    override var currentUser: AuthUser? = null
        private set
    override var restoringSession = true
        private set
    private var refreshToken: String? = null
    private val listeners = mutableSetOf<(AuthUser?) -> Unit>()
    private val mutex = Mutex()

    init {
        scope.launch {
            mutex.withLock {
                try {
                    val token = withContext(Dispatchers.IO) { store.read() }
                    if (token != null) restore(token)
                } catch (failure: CancellationException) {
                    throw failure
                } catch (_: Exception) {
                    // Offline startup leaves the saved credential intact for the next launch.
                } finally {
                    restoringSession = false
                    publish()
                }
            }
            while (true) {
                delay(50 * 60 * 1000L)
                mutex.withLock {
                    refreshToken?.let { token ->
                        try {
                            restore(token)
                        } catch (failure: CancellationException) {
                            throw failure
                        } catch (_: Exception) {
                            // Transient failures are retried; rejected credentials are cleared in restore.
                        }
                    }
                }
            }
        }
    }

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit {
        listeners.add(onChange)
        if (!restoringSession) onChange(currentUser)
        return { listeners.remove(onChange) }
    }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) =
        authenticate("signInWithPassword", mapOf("email" to email, "password" to password), complete)

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) =
        authenticate("signUp", mapOf("email" to email, "password" to password), complete)

    override fun signInWithGoogle(idToken: String, complete: (AuthError?) -> Unit) =
        authenticate(
            "signInWithIdp",
            mapOf(
                "postBody" to formBody(mapOf("id_token" to idToken, "providerId" to "google.com")),
                "requestUri" to "http://localhost",
            ),
            complete,
        )

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) =
        request(complete) {
            withContext(
                Dispatchers.IO,
            ) { api.request("sendOobCode", mapOf("requestType" to "PASSWORD_RESET", "email" to email)) }
        }

    override fun signOut(complete: (AuthError?) -> Unit) =
        request(complete) {
            withContext(Dispatchers.IO) { store.clear() }
            refreshToken = null
            currentUser = null
            publish()
        }

    private fun authenticate(method: String, values: Map<String, Any>, complete: (AuthError?) -> Unit) =
        request(complete) {
            val response = withContext(Dispatchers.IO) { api.request(method, values + ("returnSecureToken" to true)) }
            val user = AuthUser(response.get("localId").asString, response.get("email").asString)
            saveToken(response.get("refreshToken").asString)
            currentUser = user
            publish()
        }

    private suspend fun saveToken(token: String) {
        // Fail closed if credential storage cannot be updated; never retain another account's token.
        withContext(Dispatchers.IO) { store.write(token) }
        refreshToken = token
    }

    private suspend fun restore(token: String) {
        try {
            val response = withContext(Dispatchers.IO) { api.refresh(token) }
            val user =
                withContext(Dispatchers.IO) {
                    api.request("lookup", mapOf("idToken" to response.get("id_token").asString))
                        .getAsJsonArray("users").first().asJsonObject
                }
            saveToken(response.get("refresh_token").asString)
            currentUser = AuthUser(user.get("localId").asString, user.get("email").asString)
            publish()
        } catch (failure: DesktopAuthFailure) {
            if (failure.error == AuthError.CREDENTIALS) {
                refreshToken = null
                currentUser = null
                publish()
                withContext(Dispatchers.IO) { store.clear() }
            }
            throw failure
        }
    }

    private fun request(complete: (AuthError?) -> Unit, operation: suspend () -> Unit) {
        scope.launch {
            mutex.withLock {
                val error =
                    try {
                        operation()
                        null
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: DesktopAuthFailure) {
                        failure.error
                    } catch (_: IOException) {
                        AuthError.NETWORK
                    } catch (_: Exception) {
                        AuthError.UNKNOWN
                    }
                complete(error)
            }
        }
    }

    private fun publish() = listeners.toList().forEach { if (it in listeners) it(currentUser) }

    fun close() = scope.cancel()
}
