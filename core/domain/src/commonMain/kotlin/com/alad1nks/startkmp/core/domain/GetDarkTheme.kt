package com.alad1nks.startkmp.core.domain

import kotlinx.coroutines.flow.Flow

interface GetDarkTheme {
    operator fun invoke(): Flow<Boolean>
}
