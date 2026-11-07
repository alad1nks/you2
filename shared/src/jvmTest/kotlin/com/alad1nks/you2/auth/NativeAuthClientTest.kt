package com.alad1nks.you2.auth

import androidx.lifecycle.ViewModelStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeAuthClientTest {
    @Test
    fun googleTokenIsExchangedForTheNativeFirebaseSession() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        model.signInWithGoogle(nativeGoogleSignInLauncher(provider))
        assertTrue(model.state.value.busy)
        provider.googleCompletion!!("google-token", null)
        assertEquals(listOf("google", "google-token"), provider.request)
        assertTrue(model.state.value.busy)
        provider.currentUser = NativeAuthUser("google-user", "person@example.com")
        provider.observer?.invoke(provider.currentUser)
        provider.completion!!(null)
        assertEquals("google-user", model.state.value.user?.id)
        assertFalse(model.state.value.busy)
        assertNull(model.state.value.error)
    }

    @Test
    fun googleCancellationFailureAndMissingTokenAllowRetryWithoutExchangingCredentials() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        listOf(
            Triple(null, NativeGoogleSignInFailure.CANCELLED, null),
            Triple(null, NativeGoogleSignInFailure.FAILED, AuthError.GOOGLE),
            Triple(null, null, AuthError.GOOGLE),
            Triple(" ", null, AuthError.GOOGLE),
        ).forEach { (token, failure, expectedError) ->
            model.signInWithGoogle(nativeGoogleSignInLauncher(provider))
            provider.googleCompletion!!(token, failure)
            assertFalse(model.state.value.busy)
            assertEquals(expectedError, model.state.value.error)
            assertNull(provider.request)
        }
    }

    @Test
    fun googleExchangeErrorsMatchAndroidAndLateChooserResultsAreIgnored() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        NativeAuthFailure.entries.forEach { failure ->
            model.signInWithGoogle(nativeGoogleSignInLauncher(provider))
            provider.googleCompletion!!("token", null)
            provider.completion!!(failure)
            val expected =
                when (failure) {
                    NativeAuthFailure.NETWORK -> AuthError.NETWORK
                    NativeAuthFailure.TOO_MANY_REQUESTS -> AuthError.TOO_MANY_REQUESTS
                    NativeAuthFailure.EMAIL_IN_USE -> AuthError.EMAIL_IN_USE
                    else -> AuthError.GOOGLE
                }
            assertEquals(expected, model.state.value.error)
            assertFalse(model.state.value.busy)
        }
        provider.request = null
        model.signInWithGoogle(nativeGoogleSignInLauncher(provider))
        val before = model.state.value
        val store = ViewModelStore()
        store.put("auth", model)
        store.clear()
        provider.googleCompletion!!("late-token", null)
        assertNull(provider.request)
        assertEquals(before, model.state.value)
    }

    @Test
    fun restoredSessionAndFailedSignOutKeepTheActualNativeIdentity() {
        val provider = FakeNativeAuthProvider()
        provider.currentUser = NativeAuthUser("restored", "user@example.com")
        val model = AuthViewModel(NativeAuthClient(provider))
        assertEquals(AuthUser("restored", "user@example.com"), model.state.value.user)

        provider.failure = NativeAuthFailure.UNKNOWN
        model.signOut()
        assertEquals("restored", model.state.value.user?.id)
        assertEquals(AuthError.UNKNOWN, model.state.value.error)

        provider.failure = null
        model.signOut()
        assertEquals(AuthState(), model.state.value)
        assertNull(provider.currentUser)
    }

    @Test
    fun nativeRegistrationUsesSharedValidationAndPublishesTheCreatedSession() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        model.changeMode(AuthMode.REGISTER)
        model.submit("person@example.com", "password", "different")
        assertNull(provider.request)
        model.submit(" person@example.com ", " password ", " password ")
        assertEquals(listOf("register", "person@example.com", " password "), provider.request)
        assertTrue(model.state.value.busy)
        provider.currentUser = NativeAuthUser("new", "person@example.com")
        provider.observer?.invoke(provider.currentUser)
        provider.completion!!(null)
        assertEquals("new", model.state.value.user?.id)
        assertFalse(model.state.value.busy)
    }

    @Test
    fun nativeErrorsUseSharedMessagesAndResetSuccessWaitsForNativeCompletion() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        val failures =
            mapOf(
                NativeAuthFailure.EMAIL to AuthError.EMAIL,
                NativeAuthFailure.CREDENTIALS to AuthError.CREDENTIALS,
                NativeAuthFailure.WEAK_PASSWORD to AuthError.WEAK_PASSWORD,
                NativeAuthFailure.EMAIL_IN_USE to AuthError.EMAIL_IN_USE,
                NativeAuthFailure.NETWORK to AuthError.NETWORK,
                NativeAuthFailure.TOO_MANY_REQUESTS to AuthError.TOO_MANY_REQUESTS,
                NativeAuthFailure.UNKNOWN to AuthError.UNKNOWN,
            )
        failures.forEach { (native, shared) ->
            model.submit("person@example.com", "password", "")
            assertEquals("signIn", provider.request?.first())
            provider.completion!!(native)
            assertEquals(shared, model.state.value.error)
            assertFalse(model.state.value.busy)
        }
        model.changeMode(AuthMode.RESET)
        model.submit("person@example.com", "", "")
        assertEquals(listOf("reset", "person@example.com"), provider.request)
        assertFalse(model.state.value.resetSent)
        provider.completion!!(null)
        assertTrue(model.state.value.resetSent)
    }

    @Test
    fun disposalRemovesNativeListenerAndIgnoresQueuedEventsAndCompletions() {
        val provider = FakeNativeAuthProvider()
        val model = AuthViewModel(NativeAuthClient(provider))
        val store = ViewModelStore()
        store.put("auth", model)
        model.submit("person@example.com", "password", "")
        val observer = provider.observer!!
        val before = model.state.value
        store.clear()
        assertTrue(provider.unsubscribed)
        observer(NativeAuthUser("late", "person@example.com"))
        provider.completion!!(NativeAuthFailure.NETWORK)
        assertEquals(before, model.state.value)
    }
}

private class FakeNativeAuthProvider : NativeAuthProvider {
    override var currentUser: NativeAuthUser? = null
    var observer: ((NativeAuthUser?) -> Unit)? = null
    var completion: ((NativeAuthFailure?) -> Unit)? = null
    var request: List<String>? = null
    var googleCompletion: ((String?, NativeGoogleSignInFailure?) -> Unit)? = null
    var failure: NativeAuthFailure? = null
    var unsubscribed = false

    override fun observeUser(onChange: (NativeAuthUser?) -> Unit): () -> Unit {
        observer = onChange
        return {
            observer = null
            unsubscribed = true
        }
    }

    override fun signIn(email: String, password: String, complete: (NativeAuthFailure?) -> Unit) {
        request = listOf("signIn", email, password)
        completion = complete
    }

    override fun launchGoogleSignIn(complete: (String?, NativeGoogleSignInFailure?) -> Unit) {
        googleCompletion = complete
    }

    override fun signInWithGoogle(idToken: String, complete: (NativeAuthFailure?) -> Unit) {
        request = listOf("google", idToken)
        completion = complete
    }

    override fun register(email: String, password: String, complete: (NativeAuthFailure?) -> Unit) {
        request = listOf("register", email, password)
        completion = complete
    }

    override fun resetPassword(email: String, complete: (NativeAuthFailure?) -> Unit) {
        request = listOf("reset", email)
        completion = complete
    }

    override fun signOut(): NativeAuthFailure? {
        if (failure == null) {
            currentUser = null
            observer?.invoke(null)
        }
        return failure
    }
}
