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
    val registrationName: String? = null,
    val restoringSession: Boolean = false,
)

internal class AuthViewModel(private val client: AuthClient?) : ViewModel() {
    val isAvailable = client != null
    private val mutableState =
        MutableStateFlow(
            AuthState(user = client?.currentUser, restoringSession = client?.restoringSession == true),
        )
    val state = mutableState.asStateFlow()
    private var cleared = false
    private val unsubscribe =
        client?.observeUser { user ->
            if (!cleared) {
                mutableState.update {
                    it.copy(
                        user = user,
                        restoringSession = false,
                        error = null,
                        resetSent = false,
                        registrationName = it.registrationName.takeIf { _ -> it.user?.id == user?.id },
                    )
                }
            }
        }

    fun changeMode(mode: AuthMode) {
        if (!state.value.busy) mutableState.update { it.copy(mode = mode, error = null, resetSent = false) }
    }

    fun clearMessage() {
        mutableState.update { it.copy(error = null, resetSent = false) }
    }

    fun submit(email: String, password: String, confirmation: String, name: String = "") {
        if (state.value.busy || state.value.restoringSession || state.value.user != null) return
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
                        registrationName =
                            if (mode == AuthMode.REGISTER && failure == null) {
                                name.trim().takeIf { it.isNotEmpty() }
                            } else {
                                null
                            },
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

    fun registrationNameSaved() {
        mutableState.update { it.copy(registrationName = null) }
    }

    fun signOut() {
        if (cleared || state.value.busy || state.value.restoringSession) return
        val client = client ?: return
        mutableState.update { it.copy(busy = true, error = null) }
        client.signOut { failure ->
            if (!cleared) mutableState.value = AuthState(user = client.currentUser, error = failure)
        }
    }

    fun signInWithGoogle(launch: GoogleSignInLauncher) {
        val client = client ?: return
        if (cleared || state.value.busy || state.value.restoringSession ||
            state.value.user != null || state.value.mode == AuthMode.RESET
        ) {
            return
        }
        mutableState.update { it.copy(busy = true, error = null, resetSent = false) }
        var received = false
        launch { result ->
            if (!cleared && !received) {
                received = true
                when (result) {
                    GoogleSignInResult.SignedIn -> {
                        mutableState.update {
                            it.copy(
                                user = client.currentUser,
                                busy = false,
                                error = if (client.currentUser == null) AuthError.GOOGLE else null,
                            )
                        }
                    }

                    is GoogleSignInResult.Token -> {
                        client.signInWithGoogle(result.value) { error ->
                            if (!cleared) {
                                mutableState.update { it.copy(user = client.currentUser, busy = false, error = error) }
                            }
                        }
                    }

                    GoogleSignInResult.Cancelled -> {
                        mutableState.update { it.copy(busy = false) }
                    }

                    GoogleSignInResult.Failed -> {
                        mutableState.update { it.copy(busy = false, error = AuthError.GOOGLE) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        cleared = true
        unsubscribe?.invoke()
    }

    private companion object {
        val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
