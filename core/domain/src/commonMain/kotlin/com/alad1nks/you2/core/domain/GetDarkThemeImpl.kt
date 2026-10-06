package com.alad1nks.you2.core.domain

import com.alad1nks.you2.core.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GetDarkThemeImpl(
    private val settingsRepository: SettingsRepository,
) : GetDarkTheme {
    override fun invoke(): Flow<Boolean> {
        return settingsRepository.getDarkTheme().map { it == true }
    }
}
