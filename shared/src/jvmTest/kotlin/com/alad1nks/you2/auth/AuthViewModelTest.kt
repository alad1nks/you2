package com.alad1nks.you2.auth

import androidx.lifecycle.ViewModelStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthViewModelTest {
    @Test
    fun platformsWithoutAuthKeepTheirExistingFlow() {
        val model = AuthViewModel(null)
        assertFalse(model.isAvailable)
        model.submit("user@example.com", "password", "")
        assertEquals(AuthState(), model.state.value)
    }

    @Test
    fun restoresExistingSessionAndObservesSignOut() {
        val client = FakeAuthClient(AuthUser("existing", "user@example.com"))
        val model = AuthViewModel(client)
        assertEquals("existing", model.state.value.user?.id)
        client.emit(null)
        assertNull(model.state.value.user)
    }

    @Test
    fun trimsEmailButPreservesPasswordAndBlocksDuplicateRequests() {
        val client = FakeAuthClient()
        val model = AuthViewModel(client)
        model.submit(" user@example.com ", " password ", "")
        model.submit("user@example.com", "password", "")
        model.changeMode(AuthMode.REGISTER)
        assertEquals(1, client.calls)
        assertEquals("user@example.com", client.email)
        assertEquals(" password ", client.password)
        assertTrue(model.state.value.busy)
        assertEquals(AuthMode.SIGN_IN, model.state.value.mode)
        client.finish(AuthError.NETWORK)
        assertFalse(model.state.value.busy)
        assertEquals(AuthError.NETWORK, model.state.value.error)
        model.submit("user@example.com", "password", "")
        assertEquals(2, client.calls)
    }

    @Test
    fun rejectsInvalidInputWithoutSendingRequests() {
        val client = FakeAuthClient()
        val model = AuthViewModel(client)
        model.submit("invalid", "password", "")
        assertEquals(AuthError.EMAIL, model.state.value.error)
        model.submit("user@example.com", "", "")
        assertEquals(AuthError.PASSWORD, model.state.value.error)
        model.changeMode(AuthMode.REGISTER)
        model.submit("user@example.com", "short", "short")
        assertEquals(AuthError.WEAK_PASSWORD, model.state.value.error)
        model.submit("user@example.com", "password", "different")
        assertEquals(AuthError.PASSWORD_MISMATCH, model.state.value.error)
        assertEquals(0, client.calls)
    }

    @Test
    fun registrationTransitionsToRealUserAndSignOutClearsState() {
        val client = FakeAuthClient()
        val model = AuthViewModel(client)
        model.changeMode(AuthMode.REGISTER)
        model.submit("user@example.com", "password", "password")
        assertEquals(AuthMode.REGISTER, client.operation)
        client.emit(AuthUser("new-user", "user@example.com"))
        client.finish(null)
        assertEquals("new-user", model.state.value.user?.id)
        assertFalse(model.state.value.busy)
        model.signOut()
        assertNull(client.currentUser)
        assertEquals(AuthState(), model.state.value)
    }

    @Test
    fun resetDoesNotRequirePasswordAndOnlyReportsSuccessAfterCompletion() {
        val client = FakeAuthClient()
        val model = AuthViewModel(client)
        model.changeMode(AuthMode.RESET)
        model.submit("user@example.com", "", "")
        assertEquals(AuthMode.RESET, client.operation)
        assertFalse(model.state.value.resetSent)
        client.finish(AuthError.NETWORK)
        assertFalse(model.state.value.resetSent)
        model.submit("user@example.com", "", "")
        client.finish(null)
        assertTrue(model.state.value.resetSent)
        assertNull(model.state.value.user)
        model.changeMode(AuthMode.SIGN_IN)
        assertFalse(model.state.value.resetSent)
    }

    @Test
    fun clearingViewModelUnsubscribesAndIgnoresLateCompletion() {
        val client = FakeAuthClient()
        val model = AuthViewModel(client)
        val store = ViewModelStore()
        store.put("auth", model)
        model.submit("user@example.com", "password", "")
        val before = model.state.value
        store.clear()
        assertTrue(client.unsubscribed)
        client.finish(AuthError.NETWORK)
        assertEquals(before, model.state.value)
    }
}

private class FakeAuthClient(override var currentUser: AuthUser? = null) : AuthClient {
    var calls = 0
    var email = ""
    var password = ""
    var operation: AuthMode? = null
    var unsubscribed = false
    private var observer: ((AuthUser?) -> Unit)? = null
    private var completion: ((AuthError?) -> Unit)? = null

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit {
        observer = onChange
        return {
            observer = null
            unsubscribed = true
        }
    }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) {
        record(AuthMode.SIGN_IN, email, password, complete)
    }

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) {
        record(AuthMode.REGISTER, email, password, complete)
    }

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) {
        record(AuthMode.RESET, email, "", complete)
    }

    override fun signOut() = emit(null)

    fun emit(user: AuthUser?) {
        currentUser = user
        observer?.invoke(user)
    }

    fun finish(error: AuthError?) {
        val callback = completion
        completion = null
        callback?.invoke(error)
    }

    private fun record(mode: AuthMode, email: String, password: String, complete: (AuthError?) -> Unit) {
        calls++
        operation = mode
        this.email = email
        this.password = password
        completion = complete
    }
}
