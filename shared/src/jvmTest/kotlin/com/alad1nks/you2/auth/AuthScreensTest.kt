package com.alad1nks.you2.auth

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import com.alad1nks.you2.ui.LocalAppLocale
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AuthScreensTest {
    @Test
    fun phone() = capture(412, 853, false)

    @Test
    fun desktop() = capture(1280, 900, false)

    @Test
    fun darkPhone() = capture(412, 853, true)

    @Test
    fun smallPhoneLargeRussianText() = capture(320, 640, false, "ru", 1.5f)

    @Test
    fun smallPhoneLargeKazakhText() = capture(320, 640, false, "kk", 1.5f)

    private fun capture(width: Int, height: Int, dark: Boolean, locale: String = "en", fontScale: Float = 1f) =
        runDesktopComposeUiTest(width = width, height = height) {
            val state = mutableStateOf(AuthState())
            val model = AuthViewModel(null)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale), LocalAppLocale provides locale) {
                    AppTheme(darkTheme = dark) {
                        key(
                            state.value.mode,
                        ) { AuthForm(state.value, model, Language.SYSTEM, {}, {}, googleSignIn = {}) }
                    }
                }
            }
            AuthMode.entries.forEach { mode ->
                runOnIdle { state.value = AuthState(mode = mode) }
                if (mode != AuthMode.RESET) {
                    onNodeWithTag("auth-google").performScrollTo().assertIsDisplayed().assertIsEnabled()
                    if (width > 320) {
                        onRoot().captureRoboImage(
                            "src/jvmTest/screenshots/auth_${width}_${dark}_${mode.name}_METHODS.png",
                        )
                    }
                    onNodeWithTag("auth-choose-email").performScrollTo().performClick()
                } else {
                    onNodeWithTag("auth-google").assertDoesNotExist()
                }
                onNodeWithTag("auth-email").performScrollTo().assertIsDisplayed()
                if (width > 320) {
                    onRoot().captureRoboImage("src/jvmTest/screenshots/auth_${width}_${dark}_${mode.name}.png")
                }
                onNodeWithTag("auth-submit").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
                if (mode == AuthMode.REGISTER) {
                    onNodeWithTag("auth-confirmation").performScrollTo().assertIsDisplayed()
                }
                if (mode != AuthMode.RESET) {
                    val masked =
                        SemanticsMatcher("Password is visually masked") {
                            it.config[SemanticsProperties.EditableText].text == "•".repeat(15)
                        }
                    onNodeWithTag("auth-password").performScrollTo().performTextInput("sample-password")
                    onNodeWithTag("auth-password").assert(masked)
                    val toggle = onNodeWithTag("auth-password-visibility").performScrollTo()
                    if (locale == "en") toggle.assertContentDescriptionEquals("Show password")
                    toggle.performClick()
                    onNodeWithTag("auth-password").assert(masked.not())
                    if (locale == "en") toggle.assertContentDescriptionEquals("Hide password")
                    if (mode == AuthMode.REGISTER) {
                        onNodeWithTag("auth-confirmation").performTextInput("sample-password")
                        onNodeWithTag("auth-confirmation").assert(masked)
                        onNodeWithTag("auth-confirmation-visibility").performScrollTo().performClick()
                        onNodeWithTag("auth-confirmation").assert(masked.not())
                    }
                    if (width == 412 && mode == AuthMode.SIGN_IN) {
                        onRoot().captureRoboImage("src/jvmTest/screenshots/auth_${width}_${dark}_PASSWORD_VISIBLE.png")
                    }
                    toggle.performClick()
                    onNodeWithTag("auth-password").assert(masked)
                    if (mode == AuthMode.SIGN_IN) {
                        onNodeWithTag("auth-email").performScrollTo().performTextInput("alex@example.com")
                        runOnIdle { state.value = AuthState(error = AuthError.CREDENTIALS) }
                        if (width > 320) {
                            onRoot().captureRoboImage(
                                "src/jvmTest/screenshots/auth_${width}_${dark}_ERROR.png",
                            )
                        }
                        runOnIdle { state.value = AuthState(busy = true) }
                        onNodeWithTag("auth-password-visibility").assertIsNotEnabled()
                        onNodeWithTag("auth-submit").performScrollTo().assertIsNotEnabled()
                        if (width > 320) {
                            onRoot().captureRoboImage(
                                "src/jvmTest/screenshots/auth_${width}_${dark}_LOADING.png",
                            )
                        }
                    }
                }
            }
            runOnIdle { state.value = AuthState(mode = AuthMode.RESET, resetSent = true) }
            onNodeWithTag("auth-submit").performScrollTo().assertIsDisplayed()
            runOnIdle { state.value = AuthState(busy = true) }
            onNodeWithTag("auth-google").performScrollTo().assertIsNotEnabled()
            onNodeWithTag("auth-choose-email").performScrollTo().assertIsNotEnabled()
        }
}
