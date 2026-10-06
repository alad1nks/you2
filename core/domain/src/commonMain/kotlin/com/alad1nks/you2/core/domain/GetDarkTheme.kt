package com.alad1nks.you2.core.domain

import kotlinx.coroutines.flow.Flow

interface GetDarkTheme {
    operator fun invoke(): Flow<Boolean>
}
