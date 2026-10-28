package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppScreenshotTest {
    @Test
    fun lightTheme() = captureApp(darkTheme = false)

    @Test
    fun darkTheme() = captureApp(darkTheme = true)

    private fun captureApp(darkTheme: Boolean) =
        runDesktopComposeUiTest(width = 360, height = 640) {
            val theme = if (darkTheme) "dark" else "light"
            val options =
                RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0F),
                )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                    AppTheme(darkTheme = darkTheme) {
                        AppContent(greeting = "Hello, screenshot tests!")
                    }
                }
            }
            onRoot().captureRoboImage("src/jvmTest/screenshots/app_${theme}_collapsed.png", options)

            onNodeWithText("Click me!").performClick()
            mainClock.advanceTimeBy(1_000)
            waitForIdle()
            onNodeWithText("Compose: Hello, screenshot tests!").assertExists()
            onRoot().captureRoboImage("src/jvmTest/screenshots/app_${theme}_expanded.png", options)
        }
}
