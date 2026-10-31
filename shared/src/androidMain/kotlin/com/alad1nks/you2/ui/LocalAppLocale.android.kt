package com.alad1nks.you2.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = LocalConfiguration.current.locales[0].toLanguageTag()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        // Keep the host configuration intact so Compose detects changes and System
        // can always resolve the device's current language.
        val configuration = Configuration(LocalConfiguration.current)
        val locale = value?.let(Locale::forLanguageTag) ?: configuration.locales[0]
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        return LocalConfiguration.provides(configuration)
    }
}
