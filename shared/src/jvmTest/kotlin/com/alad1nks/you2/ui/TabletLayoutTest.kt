package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
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
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TabletLayoutTest {
    @Test
    fun portrait() = paired(834, 1194)

    @Test
    fun landscape() = paired(1194, 834)

    @Test
    fun landscapeDark() = paired(1194, 834, dark = true)

    private fun paired(width: Int, height: Int, dark: Boolean = false) =
        runDesktopComposeUiTest(width = width, height = height) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(
                        darkTheme = dark,
                    ) { AppContent(DemoSpace.create(), DemoDateSource.today(), darkTheme = dark) }
                }
            }
            val prefix = "src/jvmTest/screenshots/tablet_${width}_${if (dark) "dark" else "light"}"
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
            onRoot().captureRoboImage("${prefix}_details.png")
            val cover = onNodeWithTag("wish-cover").fetchSemanticsNode().boundsInRoot
            assertEquals(260f, cover.height)
            onNodeWithText("Back").performClick()
            onNodeWithText("Add a wish").performClick()
            onNodeWithTag("wish-title").performTextInput("A tablet-sized adventure")
            onRoot().captureRoboImage("${prefix}_form.png")
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Memories").performClick()
            onRoot().captureRoboImage("${prefix}_memories.png")
            onNodeWithText("Us").performClick()
            onRoot().captureRoboImage("${prefix}_us.png")
        }

    @Test
    fun guestPortrait() = guest(834, 1194)

    @Test
    fun guestLandscape() = guest(1194, 834)

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
            onRoot().captureRoboImage("src/jvmTest/screenshots/tablet_${width}_guest.png")
        }
}
