package com.alad1nks.startkmp.core.domain

import com.alad1nks.startkmp.core.model.Language

interface SetLanguage {
    suspend operator fun invoke(value: Language)
}
