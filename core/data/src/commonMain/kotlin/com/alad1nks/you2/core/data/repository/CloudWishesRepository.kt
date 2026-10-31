package com.alad1nks.you2.core.data.repository

import kotlinx.coroutines.flow.StateFlow

enum class CloudPhase { LOADING, UNPAIRED, READY, ERROR }

enum class CloudFailure { ACCESS, STORAGE_ACCESS, NETWORK, INVITE, DATA, UNKNOWN }

data class CloudState(
    val userId: String? = null,
    val displayName: String = "",
    val phase: CloudPhase = CloudPhase.LOADING,
    val inviteCode: String? = null,
    val busy: Boolean = false,
    val failure: CloudFailure? = null,
    val fromCache: Boolean = false,
    val pendingWrites: Boolean = false,
)

/** Session-aware asynchronous writes. Failures retain the operation for explicit retry. */
interface CloudWishesRepository : WishesRepository {
    val cloudState: StateFlow<CloudState>

    fun createInvitation(name: String)

    fun joinInvitation(name: String, code: String)

    fun retry()

    fun dismissFailure()

    fun close()
}
