package com.alad1nks.you2.core.domain

import com.alad1nks.you2.core.data.repository.SettingsRepository

internal class SetDarkThemeImpl(
    private val settingsRepository: SettingsRepository,
) : SetDarkTheme {
    override suspend fun invoke(value: Boolean) {
        settingsRepository.setDarkTheme(value)
    }
}
