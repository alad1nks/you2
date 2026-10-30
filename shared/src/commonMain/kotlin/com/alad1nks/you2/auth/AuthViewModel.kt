package com.alad1nks.you2.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal enum class AuthMode { SIGN_IN, REGISTER, RESET }

internal data class AuthState(
    val user: AuthUser? = null,
    val mode: AuthMode = AuthMode.SIGN_IN,
    val busy: Boolean = false,
    val error: AuthError? = null,
    val resetSent: Boolean = false,
)

internal class AuthViewModel(private val client: AuthClient?) : ViewModel() {
    val isAvailable = client != null
    private val mutableState = MutableStateFlow(AuthState(user = client?.currentUser))
    val state = mutableState.asStateFlow()
    private var cleared = false
    private val unsubscribe =
        client?.observeUser { user ->
            mutableState.update { it.copy(user = user, error = null, resetSent = false) }
        }

    fun changeMode(mode: AuthMode) {
        if (!state.value.busy) mutableState.update { it.copy(mode = mode, error = null, resetSent = false) }
    }

    fun clearMessage() {
        mutableState.update { it.copy(error = null, resetSent = false) }
    }

    fun submit(email: String, password: String, confirmation: String) {
        if (state.value.busy || state.value.user != null) return
        val client = client ?: return
        val address = email.trim()
        val mode = state.value.mode
        val error =
            when {
                !EMAIL.matches(address) -> AuthError.EMAIL
                mode != AuthMode.RESET && password.isEmpty() -> AuthError.PASSWORD
                mode == AuthMode.REGISTER && password.length < 6 -> AuthError.WEAK_PASSWORD
                mode == AuthMode.REGISTER && password != confirmation -> AuthError.PASSWORD_MISMATCH
                else -> null
            }
        if (error != null) {
            mutableState.update { it.copy(error = error, resetSent = false) }
            return
        }
        mutableState.update { it.copy(busy = true, error = null, resetSent = false) }
        val complete: (AuthError?) -> Unit = { failure ->
            if (!cleared) {
                mutableState.update {
                    it.copy(
                        user = client.currentUser,
                        busy = false,
                        error = failure,
                        resetSent = mode == AuthMode.RESET && failure == null,
                    )
                }
            }
        }
        when (mode) {
            AuthMode.SIGN_IN -> client.signIn(address, password, complete)
            AuthMode.REGISTER -> client.register(address, password, complete)
            AuthMode.RESET -> client.resetPassword(address, complete)
        }
    }

    fun signOut() {
        if (state.value.busy) return
        client?.signOut()
        mutableState.value = AuthState()
    }

    override fun onCleared() {
        cleared = true
        unsubscribe?.invoke()
    }

    private companion object {
        val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
