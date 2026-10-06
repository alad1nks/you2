package com.alad1nks.you2.core.domain

import com.alad1nks.you2.core.model.Language
import kotlinx.coroutines.flow.Flow

interface GetLanguage {
    operator fun invoke(): Flow<Language>
}
