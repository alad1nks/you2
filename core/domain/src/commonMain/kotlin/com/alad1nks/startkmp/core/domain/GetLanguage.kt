package com.alad1nks.startkmp.core.domain

import com.alad1nks.startkmp.core.model.Language
import kotlinx.coroutines.flow.Flow

interface GetLanguage {
    operator fun invoke(): Flow<Language>
}
