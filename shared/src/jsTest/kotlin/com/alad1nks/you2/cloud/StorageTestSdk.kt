@file:JsModule("firebase/storage")
@file:JsNonModule

package com.alad1nks.you2.cloud

import kotlin.js.Promise

internal external fun connectStorageEmulator(storage: dynamic, host: String, port: Int, options: dynamic)

internal external fun getMetadata(reference: dynamic): Promise<dynamic>
