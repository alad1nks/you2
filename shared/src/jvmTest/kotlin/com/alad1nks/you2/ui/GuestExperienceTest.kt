package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.AuthMode
import com.alad1nks.you2.auth.AuthUser
import com.alad1nks.you2.auth.AuthViewModel
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GuestExperienceTest {
    @Test
    fun mobilePreviews() = capturePreviews(412, 853, 1f, false)

    @Test
    fun darkPreviews() = capturePreviews(412, 853, 1f, true)

    @Test
    fun smallPhoneWithLargeText() = capturePreviews(320, 640, 1.5f, false)

    private fun capturePreviews(width: Int, height: Int, fontScale: Float, dark: Boolean) =
        runDesktopComposeUiTest(width = width, height = height) {
            var tab by mutableStateOf(MainTab.WISHES)
            var requested: AuthMode? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = dark) {
                        GuestScreen(tab, { tab = it }, Language.ENGLISH, {}, { requested = it })
                    }
                }
            }
            listOf("Wishes", "Memories", "Us").forEach { label ->
                onNodeWithText(label).performClick().assertIsSelected()
                onNodeWithTag("guest-illustration").performScrollTo().assertIsDisplayed()
                if (width == 412) {
                    onRoot().performMouseInput { moveTo(Offset.Zero) }
                    mainClock.advanceTimeBy(1_000)
                    onRoot().captureRoboImage("src/jvmTest/screenshots/guest_${label.lowercase()}_$dark.png")
                }
                onNodeWithText("Create account").performScrollTo().performClick()
                assertEquals(AuthMode.REGISTER, requested)
                onNodeWithText("Log in").performScrollTo().performClick()
                assertEquals(AuthMode.SIGN_IN, requested)
                onNodeWithText(label).assertIsDisplayed()
            }
            onNodeWithText("Add a wish").assertDoesNotExist()
            onNodeWithText("Connected").assertDoesNotExist()
            if (width == 320) onRoot().captureRoboImage("src/jvmTest/screenshots/guest_small_large_text.png")
        }

    @Test
    fun authenticationReturnsToSelectedTabAndClearsPasswords() =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val model = AuthViewModel(GuestAuthClient())
            setContent {
                val state by model.state.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) { GuestExperience(state, model, Language.ENGLISH, {}) }
                }
            }
            onNodeWithText("Memories").performClick()
            onNodeWithText("Create account").performClick()
            assertEquals(AuthMode.REGISTER, model.state.value.mode)
            onNodeWithTag("auth-email").performTextInput("guest@example.com")
            onNodeWithTag("auth-password").performTextInput("secret-password")
            onNodeWithTag("auth-confirmation").performTextInput("secret-password")
            onNodeWithText("Back").performClick()
            onNodeWithText("Memories").assertIsSelected()
            onNodeWithText("Log in").performClick()
            assertEquals(AuthMode.SIGN_IN, model.state.value.mode)
            onNodeWithTag("auth-password").assertTextEquals("Enter your password", "")
            onNodeWithTag("auth-email").assertTextEquals("you@example.com", "")
            onNodeWithText("Forgot password?").performClick()
            onNodeWithText("Reset password").assertIsDisplayed()
            onNodeWithText("Back").performClick()
            onNodeWithText("Password").assertExists()
            onNodeWithText("Back").performClick()
            onNodeWithText("Memories").assertIsSelected()
        }

    @Test
    fun languageSelectionUpdatesGuestCopyWithoutLosingTab() =
        runDesktopComposeUiTest(width = 320, height = 740) {
            var language by mutableStateOf(Language.ENGLISH)
            var tab by mutableStateOf(MainTab.US)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides language.code) {
                    AppTheme(darkTheme = false) {
                        GuestScreen(tab, { tab = it }, language, { language = it }, {})
                    }
                }
            }
            onNodeWithTag("guest-language").performClick()
            onNodeWithText("Русский").performClick()
            assertEquals(Language.RUSSIAN, language)
            onNodeWithText("Маленькие желания.\nБольше нас.").assertIsDisplayed()
            assertEquals(MainTab.US, tab)
            onNodeWithTag("guest-language").performClick()
            onNodeWithText("Қазақша").performClick()
            onNodeWithText("Кішкентай тілектер.\nБірге көбірек.").assertIsDisplayed()
            assertEquals(Language.KAZAKH, language)
            onNodeWithText("Кіру").performScrollTo().assertIsDisplayed()
        }
}

private class GuestAuthClient : AuthClient {
    override val currentUser: AuthUser? = null

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit = {}

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) = complete(null)

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) = complete(null)

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) = complete(null)

    override fun signOut() = Unit
}
