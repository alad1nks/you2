package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UsTest {
    @Test
    fun authenticatedUserCanSignOutFromUs() =
        runDesktopComposeUiTest(width = 448, height = 917) {
            var signOutCalls = 0
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(false) {
                        AppContent(DemoSpace.create(), DemoDateSource.today(), onSignOut = { signOutCalls++ })
                    }
                }
            }
            onNodeWithText("Us").performClick()
            onNodeWithTag("paired-list").performScrollToNode(hasText("Sign out"))
            onNodeWithText("Sign out").performClick()
            runOnIdle { assertEquals(1, signOutCalls) }
        }

    @Test
    fun profileDraftAndSharedCounters() =
        runDesktopComposeUiTest(width = 320, height = 917) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            setContent {
                val space by repository.space.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(false) { AppContent(space, DemoDateSource.today(), onRenameUser = repository::renameUser) }
                }
            }
            onNodeWithText("Us").performClick()
            onNodeWithTag("paired-list").performScrollToNode(hasText("Your profile"))
            onNodeWithText("Your profile").performClick()
            onNodeWithTag("profile-name").performTextReplacement("   ")
            onNodeWithText("Save").assertIsNotEnabled()
            onNodeWithTag("profile-name").performTextReplacement("A".repeat(81))
            onNodeWithText("Save").assertIsNotEnabled()
            onNodeWithTag("profile-name").performTextReplacement("Changed")
            onNodeWithText("Cancel").performClick()
            runOnIdle { assertEquals("Alex", repository.space.value.couple.you.name) }
            onNodeWithText("Your profile").performClick()
            onNodeWithTag("profile-name").assertTextContains("Alex")
            onNodeWithTag("profile-name").performTextReplacement("  Sam  ")
            onNodeWithText("Save").performClick()
            runOnIdle { assertEquals("Sam", repository.space.value.couple.you.name) }
            onNodeWithText("S + M").assertExists()
            val wish = repository.space.value.active.first()
            runOnIdle { repository.complete(wish.id) }
            onNodeWithTag("paired-list").performScrollToNode(hasText("Wishes to try"))
            onNode(hasText("11") and hasAnyAncestor(androidx.compose.ui.test.hasTestTag("us-wishes"))).assertExists()
            onNode(hasText("5") and hasAnyAncestor(androidx.compose.ui.test.hasTestTag("us-memories"))).assertExists()
            runOnIdle {
                repository.restore(wish.id)
                repository.delete(wish.id)
            }
            onNode(hasText("4") and hasAnyAncestor(androidx.compose.ui.test.hasTestTag("us-memories"))).assertExists()
            onNodeWithTag("paired-list").performScrollToNode(hasText("Sign out"))
            onNodeWithText("Sign out").assertIsNotEnabled()
            onNodeWithText("Sign out is unavailable in this paired demo.").assertIsDisplayed()
        }

    @Test
    fun settingsChangeVisibleLanguageAndTheme() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            var language by mutableStateOf(Language.ENGLISH)
            var dark by mutableStateOf(false)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides language.code) {
                    AppTheme(dark) {
                        AppContent(
                            DemoSpace.create(),
                            DemoDateSource.today(),
                            darkTheme = dark,
                            language = language,
                            onSetDarkTheme = { dark = it },
                            onSetLanguage = { language = it },
                        )
                    }
                }
            }
            onNodeWithText("Us").performClick()
            onNodeWithText("Appearance").performClick()
            onNodeWithText("Dark").performClick()
            runOnIdle { assertEquals(true, dark) }
            onNodeWithText("Dark").assertExists()
            onNodeWithText("Language").performClick()
            onNodeWithText("Русский").performClick()
            onNodeWithText("Ваш профиль").assertIsDisplayed()
            onNodeWithText("Язык").performClick()
            onNodeWithText("Қазақша").performClick()
            onNodeWithText("Профиліңіз").assertIsDisplayed()
            onNodeWithText("Тіл").performClick()
            onNodeWithText("English").performClick()
            onNodeWithText("Your profile").assertIsDisplayed()
            onNodeWithText("Language").performClick()
            onNodeWithText("System default").performClick()
            runOnIdle { assertEquals(Language.SYSTEM, language) }
        }

    @Test
    fun lightUs() = captureUs(false, 412, Language.ENGLISH)

    @Test
    fun darkUs() = captureUs(true, 412, Language.ENGLISH)

    @Test
    fun narrowRussianUs() = captureUs(false, 320, Language.RUSSIAN)

    @Test
    fun narrowKazakhUs() = captureUs(false, 320, Language.KAZAKH)

    private fun captureUs(dark: Boolean, width: Int, language: Language) =
        runDesktopComposeUiTest(width = width, height = 917) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides language.code) {
                    AppTheme(
                        dark,
                    ) { AppContent(DemoSpace.create(), DemoDateSource.today(), darkTheme = dark, language = language) }
                }
            }
            val us =
                when (language) {
                    Language.RUSSIAN -> "Мы"
                    Language.KAZAKH -> "Біз"
                    else -> "Us"
                }
            val profile =
                when (language) {
                    Language.RUSSIAN -> "Ваш профиль"
                    Language.KAZAKH -> "Профиліңіз"
                    else -> "Your profile"
                }
            val cancel =
                when (language) {
                    Language.RUSSIAN -> "Отмена"
                    Language.KAZAKH -> "Бас тарту"
                    else -> "Cancel"
                }
            val appearance =
                when (language) {
                    Language.RUSSIAN -> "Оформление"
                    Language.KAZAKH -> "Сыртқы көрініс"
                    else -> "Appearance"
                }
            val languageLabel =
                when (language) {
                    Language.RUSSIAN -> "Язык"
                    Language.KAZAKH -> "Тіл"
                    else -> "Language"
                }
            val prefix = "src/jvmTest/screenshots/us_${if (dark) "dark" else "light"}_${width}_${language.code}"
            onNodeWithText(us).performClick()
            onRoot().captureRoboImage("$prefix.png")
            listOf(profile, appearance, languageLabel).forEachIndexed { index, label ->
                onNodeWithTag("paired-list").performScrollToNode(hasText(label))
                onNodeWithText(label).performClick()
                onAllNodes(isRoot()).onLast().captureRoboImage("${prefix}_dialog_$index.png")
                onNodeWithText(cancel).performClick()
            }
            val signOut =
                when (language) {
                    Language.RUSSIAN -> "Выйти"
                    Language.KAZAKH -> "Шығу"
                    else -> "Sign out"
                }
            onNodeWithTag("paired-list").performScrollToNode(hasText(signOut))
            onRoot().captureRoboImage("${prefix}_end.png")
        }
}
