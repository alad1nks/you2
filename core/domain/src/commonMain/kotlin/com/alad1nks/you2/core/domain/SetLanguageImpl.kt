package com.alad1nks.you2.core.domain

import com.alad1nks.you2.core.data.repository.SettingsRepository
import com.alad1nks.you2.core.model.Language

internal class SetLanguageImpl(
    private val settingsRepository: SettingsRepository,
) : SetLanguage {
    override suspend fun invoke(value: Language) {
        settingsRepository.setLanguage(value)
    }
}
