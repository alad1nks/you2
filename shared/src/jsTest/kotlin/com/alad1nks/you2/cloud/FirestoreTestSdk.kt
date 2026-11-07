@file:JsModule("firebase/firestore")
@file:JsNonModule

package com.alad1nks.you2.cloud

import kotlin.js.Promise

internal external fun connectFirestoreEmulator(db: dynamic, host: String, port: Int, options: dynamic)

internal external fun getDoc(reference: dynamic): Promise<dynamic>

internal external fun terminate(db: dynamic): Promise<Unit>

internal external fun disableNetwork(db: dynamic): Promise<Unit>

internal external fun enableNetwork(db: dynamic): Promise<Unit>
