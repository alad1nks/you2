package com.alad1nks.you2.cloud

/** Platform cloud boundary. Calls/callbacks run on main; unsubscribe suppresses queued events. */
interface NativeCloudProvider {
    fun observe(userId: String, onChange: (NativeCloudSnapshot) -> Unit): () -> Unit

    fun newId(): String

    /** Payloads use WishDocument's schema. Null map values represent Firestore nulls. */
    fun write(
        operation: String,
        userId: String,
        coupleId: String?,
        documentId: String,
        fields: Map<String, Any?>,
        complete: (NativeCloudFailure?) -> Unit,
    )
}

enum class NativeCloudFailure { ACCESS, STORAGE_ACCESS, NETWORK, INVITE, DATA, UNKNOWN }

enum class NativeCloudPhase { LOADING, UNPAIRED, READY, ERROR }

/** Native snapshots omit null dictionary entries recursively, preserving missing nullable fields. */
data class NativeCloudDocument(val id: String, val fields: Map<String, Any?>)

data class NativeCloudSnapshot(
    val phase: NativeCloudPhase,
    val displayName: String,
    val coupleId: String?,
    val inviteCode: String?,
    val partnerId: String?,
    val partnerName: String?,
    val wishes: List<NativeCloudDocument>,
    val fromCache: Boolean,
    val pendingWrites: Boolean,
    val failure: NativeCloudFailure?,
)
