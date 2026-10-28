package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.feature.designsystem.components.WishCover
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppScreenshotTest {
    @Test
    fun emptyListAndEmptyCategory() =
        runDesktopComposeUiTest(width = 320, height = 917) {
            val initial = DemoSpace.create()
            var space by androidx.compose.runtime.mutableStateOf(
                initial.copy(wishes = initial.wishes.filterNot { it.content.category == WishCategory.ADVENTURES }),
            )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) { AppContent(space, DemoDateSource.today()) }
                }
            }
            onNodeWithText("Adventures").performClick()
            onNodeWithText("No wishes in this category yet.").assertIsDisplayed()
            onNodeWithText("Show all wishes").performClick()
            onNodeWithText("Finally watch About Time").assertIsDisplayed()
            runOnIdle { space = initial.copy(wishes = initial.memories) }
            onNodeWithText("Your shared list is waiting for its first wish.").assertIsDisplayed()
            onNodeWithText("Add a wish").performClick()
            onNodeWithText("Wish title").assertExists()
        }

    @Test
    fun longContentAndAllDateStates() =
        runDesktopComposeUiTest(width = 320, height = 917) {
            val initial = DemoSpace.create()
            val longTitle = "A long weekend together discovering our favorite places and making new memories"
            val longDescription = "Bring our favorite snacks and a blanket. ".repeat(12)
            val space =
                initial.copy(
                    wishes =
                        initial.wishes.mapIndexed { index, wish ->
                            if (index == 11) {
                                wish.copy(
                                    content =
                                        wish.content.copy(
                                            title = longTitle,
                                            description = longDescription,
                                            dates = WishDates.Range(LocalDate(2026, 12, 31), LocalDate(2027, 1, 2)),
                                        ),
                                )
                            } else {
                                wish
                            }
                        },
                )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) { AppContent(space, DemoDateSource.today()) }
                }
            }
            listOf(
                "Due today · 28 Oct 2026",
                "Due 16 Nov 2026",
                "Overdue · 26 Oct 2026",
                "14–16 Nov 2026 · Upcoming",
                "26–30 Oct 2026 · In progress",
                "20–24 Oct 2026 · Ended",
                "31 Oct 2026 – 1 Nov 2026 · Upcoming",
            ).forEach { label ->
                onNodeWithTag("paired-list").performScrollToNode(hasText(label))
                onNodeWithText(label).assertIsDisplayed()
                onNodeWithText("Add a wish").assertIsDisplayed()
                onNodeWithText("you2").assertIsDisplayed()
            }
            onNodeWithTag("paired-list").performScrollToNode(hasText("31 Dec 2026 – 2 Jan 2027 · Upcoming"))
            onNodeWithText("31 Dec 2026 – 2 Jan 2027 · Upcoming").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/wishes_long_320.png")
            onNodeWithTag("wish-5").performClick()
            onNode(hasText(longTitle) and hasAnyAncestor(isDialog())).assertExists()
            onNodeWithText("Back").performClick()
            onNodeWithText("31 Dec 2026 – 2 Jan 2027 · Upcoming").assertIsDisplayed()
        }

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
            onNodeWithText("Two films, one very cozy evening.").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/paired_${theme}_${width}_wishes_end.png", options)
            onNodeWithTag("paired-list").performScrollToIndex(0)
            onNodeWithTag("wish-sort").performClick()
            // Dismiss through the menu item; only the specified newest-first mode is available.
            onAllNodes(hasText("Newest first ↓"))[1].performClick()
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
