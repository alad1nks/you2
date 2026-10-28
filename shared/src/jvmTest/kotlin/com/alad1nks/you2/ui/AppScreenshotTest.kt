package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.feature.designsystem.components.WishCover
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppScreenshotTest {
    @Test
    fun categoryIllustrations() =
        runDesktopComposeUiTest(width = 412, height = 1100) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                    AppTheme(darkTheme = false) {
                        Surface {
                            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                WishCategory.entries.forEach { WishCover(it.image) }
                            }
                        }
                    }
                }
            }
            waitForIdle()
            onRoot().captureRoboImage("src/jvmTest/screenshots/category_illustrations.png")
        }

    @Test
    fun lightTheme() = captureApp(darkTheme = false, width = 412)

    @Test
    fun darkTheme() = captureApp(darkTheme = true, width = 412)

    @Test
    fun narrowScreen() = captureApp(darkTheme = false, width = 320)

    private fun captureApp(darkTheme: Boolean, width: Int) =
        runDesktopComposeUiTest(width = width, height = 917) {
            val theme = if (darkTheme) "dark" else "light"
            val options =
                RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0F),
                )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = darkTheme) {
                        AppContent(DemoSpace.create(), DemoDateSource.today())
                    }
                }
            }
            waitForIdle()
            onRoot().captureRoboImage("src/jvmTest/screenshots/paired_${theme}_${width}_wishes.png", options)
            onNodeWithTag("paired-list").performScrollToNode(hasText("A classic movie marathon"))
            onNodeWithText("A classic movie marathon").performClick()
            onNodeWithText("Back").performClick()
            onNodeWithText("A classic movie marathon").assertIsDisplayed()
            onNodeWithTag("paired-list").performScrollToNode(hasText("All wishes"))
            onNodeWithText("Presents").performClick()
            onNodeWithText("A little gift, just because").performClick()
            onNodeWithText("Back").performClick()
            onNodeWithText("A book for each other").assertExists()
            onNodeWithText("A weekend in the mountains").assertDoesNotExist()
            onNodeWithText("Memories").performClick()
            onNodeWithText("Our memories").assertExists()
            onNodeWithText("Us").performClick()
            onNodeWithText("Connected").assertExists()
            waitForIdle()
            onRoot().captureRoboImage("src/jvmTest/screenshots/paired_${theme}_${width}_us.png", options)
            onNodeWithText("Wishes").performClick()
            onNodeWithText("A book for each other").assertExists()
            onNodeWithText("Add a wish").performClick()
            onNodeWithText("Wish title").assertExists()
            onNodeWithText("Back").performClick()
        }
}
