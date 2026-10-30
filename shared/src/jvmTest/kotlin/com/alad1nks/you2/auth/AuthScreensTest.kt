package com.alad1nks.you2.auth

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
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
                        AuthForm(state.value, model, Language.SYSTEM, {}, {})
                    }
                }
            }
            AuthMode.entries.forEach { mode ->
                runOnIdle { state.value = AuthState(mode = mode) }
                onNodeWithTag("auth-email").performScrollTo().assertIsDisplayed()
                if (width > 320) {
                    onRoot().captureRoboImage("src/jvmTest/screenshots/auth_${width}_${dark}_${mode.name}.png")
                }
                onNodeWithTag("auth-submit").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
                if (mode == AuthMode.REGISTER) {
                    onNodeWithTag("auth-confirmation").performScrollTo().assertIsDisplayed()
                }
            }
            runOnIdle { state.value = AuthState(mode = AuthMode.RESET, resetSent = true) }
            onNodeWithTag("auth-submit").performScrollTo().assertIsDisplayed()
        }
}
