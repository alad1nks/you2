package com.alad1nks.startkmp.core.domain

interface SetDarkTheme {
    suspend operator fun invoke(value: Boolean)
}
