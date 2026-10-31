package com.alad1nks.you2.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.wishes
import org.jetbrains.compose.resources.stringResource
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class LocalAppLocaleTest {
    @Test
    fun changingLanguageRefreshesStringsAndRestoresSystemLocale() =
        runComposeUiTest {
            val originalLocale = Locale.getDefault()
            val language = mutableStateOf<String?>(null)
            try {
                setContent {
                    val systemConfiguration = LocalConfiguration.current
                    CompositionLocalProvider(LocalAppLocale provides language.value) {
                        Text(stringResource(Res.string.wishes))
                    }
                    // Applying an app override must not mutate the parent's configuration.
                    assertEquals("en", systemConfiguration.locales[0].language)
                }
                onNodeWithText("Wishes").assertIsDisplayed()
                runOnIdle { language.value = "ru" }
                onNodeWithText("Желания").assertIsDisplayed()
                runOnIdle { language.value = "kk" }
                onNodeWithText("Тілектер").assertIsDisplayed()
                runOnIdle { language.value = "en" }
                onNodeWithText("Wishes").assertIsDisplayed()
                runOnIdle { language.value = "ru" }
                onNodeWithText("Желания").assertIsDisplayed()
                runOnIdle { language.value = null }
                onNodeWithText("Wishes").assertIsDisplayed()
            } finally {
                Locale.setDefault(originalLocale)
            }
        }
}
