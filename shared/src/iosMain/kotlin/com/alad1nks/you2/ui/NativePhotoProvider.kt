package com.alad1nks.you2.ui

import platform.Foundation.NSData

/** Native callbacks run on main. Disposing a request suppresses subsequent callbacks. */
interface NativePhotoProvider {
    fun pick(maxImages: Int, complete: (List<String>?) -> Unit): () -> Unit

    fun load(source: String, complete: (NSData?) -> Unit): () -> Unit
}

interface NativeSystemBarsProvider {
    fun setHidden(hidden: Boolean)
}
