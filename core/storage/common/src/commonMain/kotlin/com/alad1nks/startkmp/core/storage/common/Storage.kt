package com.alad1nks.startkmp.core.storage.common

import kotlinx.coroutines.flow.Flow

interface Storage {
    fun getDarkTheme(): Flow<Boolean?>

    fun getLanguageCode(): Flow<String?>

    suspend fun setDarkTheme(value: Boolean)

    suspend fun setLanguageCode(value: String)
}
