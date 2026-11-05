package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class WishDetailsTest {
    @Test
    fun cancelEditDeleteAndCompleteUseSharedState() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            val initial = repository.space.value
            setContent {
                val space by repository.space.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        AppContent(space, DemoDateSource.today(), repository::delete, repository::complete)
                    }
                }
            }
            onNodeWithText("Presents").performClick()
            onNodeWithText("A little gift, just because").performClick()
            onNodeWithTag("paired-list").assertDoesNotExist()
            onNode(isDialog()).assertDoesNotExist()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Delete wish").performClick()
            onNodeWithText(
                "“A little gift, just because” will be removed from the shared list for both you and Mia.",
            ).assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            runOnIdle { assertEquals(initial, repository.space.value) }
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit wish").performClick()
            onNode(hasText("A little gift, just because") and hasAnyAncestor(hasTestTag("wish-form"))).assertExists()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Mark as done").assertIsDisplayed()
            runOnIdle { assertEquals(initial, repository.space.value) }
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Delete wish").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithText("A little gift, just because").assertDoesNotExist()
            onNodeWithText("A book for each other").assertIsDisplayed()
            onNodeWithText("A weekend in the mountains").assertDoesNotExist()
            runOnIdle { assertEquals(11, repository.space.value.active.size) }
            onNodeWithText("A book for each other").performClick()
            onNodeWithText("Mark as done").performClick()
            onNodeWithText("No wishes in this category yet.").assertIsDisplayed()
            onNodeWithText("Memories").performClick()
            onNodeWithTag("paired-list").performScrollToNode(hasText("A book for each other"))
            onNodeWithText("A book for each other").assertIsDisplayed()
            runOnIdle {
                val completed = repository.space.value.memories.single { it.content.title == "A book for each other" }
                assertEquals(DemoDateSource.today(), completed.completion!!.date)
                assertEquals("alex", completed.completion!!.authorId)
                assertEquals(10, repository.space.value.active.size)
                assertEquals(5, repository.space.value.memories.size)
            }
        }

    @Test
    fun lightDetails() = captureDetails(false, 412)

    @Test
    fun darkDetails() = captureDetails(true, 412)

    @Test
    fun narrowDetails() = captureDetails(false, 320)

    private fun captureDetails(dark: Boolean, width: Int) =
        runDesktopComposeUiTest(width = width, height = 917) {
            val space = DemoSpace.create()
            val wish =
                space.active.first().copy(
                    content =
                        space.active.first().content.copy(
                            title = "A weekend by the sea",
                            description =
                                "Find a little place by the coast. " +
                                    "Slow mornings, long walks, and nowhere we need to be.",
                        ),
                )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(
                        darkTheme = dark,
                    ) { WishDetails(wish, space.couple, DemoDateSource.today(), {}, {}, {}, {}) }
                }
            }
            val prefix = "src/jvmTest/screenshots/details_${if (dark) "dark" else "light"}_$width"
            onRoot().captureRoboImage("$prefix.png")
            onNodeWithContentDescription("More options").performClick()
            onAllNodes(isRoot())[1].captureRoboImage("${prefix}_menu.png")
            onAllNodes(isRoot())[1].performKeyInput { pressKey(Key.Escape) }
            onNodeWithText("Edit wish").assertDoesNotExist()
            onNodeWithContentDescription("More options").performClick()
            onAllNodes(isRoot())[1].performTouchInput { click(Offset(4f, 500f)) }
            onNodeWithText("Edit wish").assertDoesNotExist()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Delete wish").performClick()
            onAllNodes(isRoot())[1].captureRoboImage("${prefix}_delete.png")
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Mark as done").assertIsDisplayed()
        }

    @Test
    fun categoriesEmptyDescriptionLongContentAndAttachments() =
        runDesktopComposeUiTest(width = 320, height = 917) {
            val space = DemoSpace.create()
            var selected by androidx.compose.runtime.mutableStateOf(space.active.first())
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(
                        darkTheme = false,
                    ) { WishDetails(selected, space.couple, DemoDateSource.today(), {}, {}, {}, {}) }
                }
            }
            WishCategory.entries.forEach { category ->
                runOnIdle { selected = space.active.first { it.content.category == category } }
                onNodeWithText("Waiting to happen").assertExists()
                onNodeWithText("Mark as done").assertIsDisplayed()
                onRoot().captureRoboImage("src/jvmTest/screenshots/details_category_${category.name.lowercase()}.png")
            }
            runOnIdle {
                selected =
                    selected.copy(
                        content = selected.content.copy(description = "", dates = WishDates.NoDate),
                    )
            }
            onNodeWithText("The little details").assertDoesNotExist()
            runOnIdle {
                selected =
                    space.active.single { it.content.attachments.isNotEmpty() }.let {
                        it.copy(
                            content =
                                it.content.copy(
                                    title =
                                        "A long weekend together discovering our favorite places " +
                                            "and making new memories",
                                    description = "Bring our favorite snacks and a blanket. ".repeat(12),
                                ),
                        )
                    }
            }
            onNodeWithTag("wish-details").performScrollToNode(hasText("14–16 Nov 2026 · Upcoming"))
            onNodeWithText("14–16 Nov 2026 · Upcoming").assertIsDisplayed()
            onNodeWithTag("wish-details").performScrollToNode(androidx.compose.ui.test.hasTestTag("wish-attachment-1"))
            onNodeWithTag("wish-attachment-1").assertIsDisplayed()
            onNodeWithTag("wish-details").performScrollToNode(hasText("Mark it as done to move it to Memories."))
            onNodeWithText("Mark as done").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/details_long_end_320.png")
            runOnIdle { assertFalse(selected.content.attachments.isEmpty()) }
        }
}
