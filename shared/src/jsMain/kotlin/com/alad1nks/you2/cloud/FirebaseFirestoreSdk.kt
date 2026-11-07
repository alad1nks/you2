@file:JsModule("firebase/firestore")
@file:JsNonModule

package com.alad1nks.you2.cloud

import kotlin.js.Promise

internal external fun getFirestore(app: dynamic): dynamic

internal external fun doc(db: dynamic, path: String): dynamic

internal external fun collection(db: dynamic, path: String): dynamic

internal external fun query(reference: dynamic, constraint: dynamic): dynamic

internal external fun orderBy(field: String, direction: String): dynamic

internal external fun serverTimestamp(): dynamic

internal external fun arrayUnion(value: String): dynamic

internal external class Timestamp {
    companion object {
        fun fromMillis(milliseconds: Double): dynamic
    }
}

internal external fun onSnapshot(
    reference: dynamic,
    options: dynamic,
    next: (dynamic) -> Unit,
    error: (dynamic) -> Unit,
): () -> Unit

internal external fun runTransaction(db: dynamic, update: (WebTransaction) -> Promise<Unit>): Promise<Unit>

internal external interface WebTransaction {
    fun get(reference: dynamic): Promise<dynamic>

    fun set(reference: dynamic, data: dynamic): WebTransaction

    fun update(reference: dynamic, data: dynamic): WebTransaction

    fun delete(reference: dynamic): WebTransaction
}
