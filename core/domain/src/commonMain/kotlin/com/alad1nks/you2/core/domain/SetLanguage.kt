package com.alad1nks.you2.core.domain

import com.alad1nks.you2.core.model.Language

interface SetLanguage {
    suspend operator fun invoke(value: Language)
}
