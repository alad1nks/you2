package com.alad1nks.you2.core.domain

interface SetDarkTheme {
    suspend operator fun invoke(value: Boolean)
}
