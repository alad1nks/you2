package com.alad1nks.you2.ui

import com.alad1nks.you2.core.data.repository.SettingsRepository
import com.alad1nks.you2.core.domain.GetDarkTheme
import com.alad1nks.you2.core.domain.GetLanguage
import com.alad1nks.you2.core.domain.SetDarkTheme
import com.alad1nks.you2.core.domain.SetLanguage
import com.alad1nks.you2.core.domain.di.DomainModule
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.core.storage.common.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsTest {
    @Test
    fun settingsSurviveRecreatingRepositoryAndDomainWithTheSameStorage() =
        runBlocking {
            val storage = TestStorage()

            fun newScope() =
                koinApplication {
                    modules(DomainModule, module { single { SettingsRepository(storage) } })
                }
            val first = newScope()
            try {
                first.koin.get<SetDarkTheme>()(true)
                first.koin.get<SetLanguage>()(Language.KAZAKH)
            } finally {
                first.close()
            }
            val reopened = newScope()
            try {
                assertEquals(true, reopened.koin.get<GetDarkTheme>()().first())
                assertEquals(Language.KAZAKH, reopened.koin.get<GetLanguage>()().first())
                reopened.koin.get<SetLanguage>()(Language.SYSTEM)
                assertEquals("system", storage.language.value)
                assertEquals(Language.SYSTEM, reopened.koin.get<GetLanguage>()().first())
                reopened.koin.get<SetDarkTheme>()(false)
                assertEquals(false, reopened.koin.get<GetDarkTheme>()().first())
                storage.language.value = "unsupported"
                assertEquals(Language.SYSTEM, reopened.koin.get<GetLanguage>()().first())
            } finally {
                reopened.close()
            }
        }

    private class TestStorage : Storage {
        val language = MutableStateFlow<String?>(null)
        private val dark = MutableStateFlow<Boolean?>(null)

        override fun getDarkTheme() = dark

        override fun getLanguageCode() = language

        override suspend fun setDarkTheme(value: Boolean) {
            dark.value = value
        }

        override suspend fun setLanguageCode(value: String) {
            language.value = value
        }
    }
}
