@file:JsModule("firebase/storage")
@file:JsNonModule

package com.alad1nks.you2.cloud

import kotlin.js.Promise

internal external fun getStorage(app: dynamic): dynamic

internal external fun ref(storage: dynamic, path: String): dynamic

internal external fun uploadString(
    reference: dynamic,
    value: String,
    format: String,
    metadata: dynamic,
): Promise<dynamic>

internal external fun getBytes(reference: dynamic, maxDownloadSizeBytes: Int): Promise<dynamic>

internal external fun deleteObject(reference: dynamic): Promise<Unit>
