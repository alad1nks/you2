package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ImageLoadingTest {
    @Test
    fun cover() = capture(364, 160, showLabel = true)

    @Test
    fun attachment() = capture(144, 96, showLabel = false)

    @Test
    fun viewer() = capture(412, 917, showLabel = true, viewer = true)

    private fun capture(width: Int, height: Int, showLabel: Boolean, viewer: Boolean = false) =
        runDesktopComposeUiTest(width = width, height = height) {
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = viewer) {
                        ImageLoadingPlaceholder(Modifier.fillMaxSize(), showLabel, viewer)
                    }
                }
            }
            mainClock.advanceTimeByFrame()
            onNodeWithTag("image-loading").assertIsDisplayed()
            if (showLabel) {
                onNodeWithText("Loading image…").assertIsDisplayed()
            } else {
                onNodeWithText("Loading image…").assertDoesNotExist()
            }
            onNodeWithTag("image-loading").captureRoboImage(
                "src/jvmTest/screenshots/image_loading_$width.png",
            )
        }
}
