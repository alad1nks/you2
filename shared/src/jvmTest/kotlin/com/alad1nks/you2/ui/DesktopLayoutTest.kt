package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesktopLayoutTest {
    @Test
    fun resizePreservesOpenForm() =
        runDesktopComposeUiTest(width = 1440, height = 900) {
            val width = mutableStateOf(1280.dp)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) {
                        Box(Modifier.width(width.value)) { AppContent(DemoSpace.create(), DemoDateSource.today()) }
                    }
                }
            }
            onNodeWithText("Add a wish").performClick()
            onNodeWithTag("wish-title").performTextInput("Keep this draft")
            runOnIdle { width.value = 412.dp }
            onNodeWithTag("desktop-navigation").assertDoesNotExist()
            onNodeWithText("Keep this draft").assertIsDisplayed()
            runOnIdle { width.value = 1280.dp }
            onNodeWithTag("desktop-navigation").assertIsDisplayed()
            onNodeWithText("Keep this draft").assertIsDisplayed()
        }

    @Test
    fun desktop() = paired(1280, 900)

    @Test
    fun wideDesktop() = paired(1440, 900)

    @Test
    fun desktopDark() = paired(1280, 900, dark = true)

    private fun paired(width: Int, height: Int, dark: Boolean = false) =
        runDesktopComposeUiTest(width = width, height = height) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(
                        darkTheme = dark,
                    ) { AppContent(DemoSpace.create(), DemoDateSource.today(), darkTheme = dark) }
                }
            }
            val prefix = "src/jvmTest/screenshots/desktop_${width}_${if (dark) "dark" else "light"}"
            onNodeWithTag("desktop-navigation").assertIsDisplayed()
            val rail = onNodeWithTag("desktop-navigation").fetchSemanticsNode().boundsInRoot
            assertEquals(104f, rail.width)
            val add = onNodeWithText("Add a wish").fetchSemanticsNode().boundsInRoot
            assertTrue(add.top < 160f)
            val first = onNodeWithTag("wish-16").fetchSemanticsNode().boundsInRoot
            // The first two wishes share a row; this fails if the phone list is merely stretched.
            val second = onNodeWithText("Finally watch About Time").fetchSemanticsNode().boundsInRoot
            assertTrue(second.left > first.left)
            onRoot().captureRoboImage("${prefix}_wishes.png")
            onNodeWithText("Movies").performClick()
            onNodeWithText("Finally watch About Time").assertIsDisplayed()
            onNodeWithText("All wishes").performClick()
            onNodeWithTag("paired-list").performScrollToNode(hasText("A weekend by the sea"))
            onNodeWithText("A weekend by the sea").performClick()
            onNodeWithTag("desktop-navigation").assertIsDisplayed()
            onRoot().captureRoboImage("${prefix}_details.png")
            val cover = onNodeWithTag("wish-cover").fetchSemanticsNode().boundsInRoot
            assertEquals(260f, cover.height)
            onNodeWithText("Back").performClick()
            onNodeWithText("Add a wish").performClick()
            onNodeWithTag("wish-title").performTextInput("A desktop adventure")
            onRoot().captureRoboImage("${prefix}_form.png")
            onNodeWithTag("desktop-navigation").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Memories").performClick()
            onRoot().captureRoboImage("${prefix}_memories.png")
            onNodeWithText("Us").performClick()
            onRoot().captureRoboImage("${prefix}_us.png")
            onNodeWithText("Wishes").performClick()
            onNodeWithText("Add a wish").performClick()
            onNodeWithText("Memories").performClick()
            onNodeWithTag("wish-form").assertDoesNotExist()
            onNodeWithText("Once a wish. Now a story we share.").assertIsDisplayed()
        }

    @Test
    fun guestDesktop() = guest(1280, 900)

    @Test
    fun guestWideDesktop() = guest(1440, 900)

    private fun guest(width: Int, height: Int) =
        runDesktopComposeUiTest(width = width, height = height) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        GuestScreen(MainTab.US, {}, Language.ENGLISH, {}, {})
                    }
                }
            }
            onNodeWithText("Create account").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/desktop_${width}_guest.png")
        }
}
