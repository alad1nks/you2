package com.alad1nks.you2.core.storage.js

internal external interface BrowserStorage {
    fun getItem(key: String): String?

    fun setItem(key: String, value: String)

    fun removeItem(key: String)

    fun clear()
}
