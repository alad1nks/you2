package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class WishFormTest {
    @Test
    fun createEditAndCancelUpdateOnlyOnSave() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            setContent {
                val space by repository.space.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        AppContent(
                            space,
                            DemoDateSource.today(),
                            repository::delete,
                            repository::complete,
                            onSaveWish = {
                                id,
                                draft,
                                ->
                                if (id == null) repository.create(draft) else repository.edit(id, draft)
                            },
                        )
                    }
                }
            }
            onNodeWithText("Add a wish").performClick()
            onNodeWithTag("save-wish").assertIsNotEnabled()
            onNodeWithTag("wish-title").performTextReplacement("A new shared idea")
            onNodeWithTag("save-wish").assertIsEnabled()
            onNodeWithText("Deadline").performClick()
            onNodeWithTag("save-wish").assertIsNotEnabled()
            onNodeWithText("Choose date").performClick()
            onNodeWithText("Wednesday, October 28, 2026").performClick()
            onNode(hasText("Select date") and hasClickAction()).performClick()
            onNodeWithTag("save-wish").assertIsEnabled()
            onNodeWithText("No date").performClick()
            onNodeWithTag("wish-form").performScrollToNode(hasText("Add main image"))
            onNodeWithText("Add main image").performClick()
            onNodeWithTag("demo-image-PLANS").performClick()
            onNodeWithText("Use selected images").performClick()
            onNodeWithTag("wish-form").performScrollToNode(hasText("Add images"))
            onNodeWithText("Add images").performClick()
            onNodeWithTag("demo-image-ADVENTURES").performClick()
            onNodeWithTag("demo-image-PLANS").performClick()
            onNodeWithText("Use selected images").performClick()
            onNodeWithTag("wish-form").performScrollToNode(hasTestTag("form-attachment-0"))
            onNodeWithContentDescription("Remove attachment 1").performClick()
            onNodeWithTag("save-wish").performClick()
            onNodeWithText("A new shared idea").performClick()
            runOnIdle {
                val created = repository.space.value.active.first()
                assertEquals(13, repository.space.value.active.size)
                assertEquals(WishImage(WishCategory.PLANS), created.content.cover)
                assertEquals(listOf(WishImage(WishCategory.PLANS)), created.content.attachments)
                assertEquals(WishDates.NoDate, created.content.dates)
            }
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit wish").performClick()
            onNodeWithTag("wish-title").performTextReplacement("Discard this edit")
            onNodeWithText("Cancel").performClick()
            onNode(hasText("A new shared idea") and hasAnyAncestor(isDialog())).assertExists()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit wish").performClick()
            onNodeWithTag("wish-title").performTextReplacement("Saved edit")
            onNodeWithTag("wish-form").performScrollToNode(hasText("Remove image"))
            onNodeWithText("Remove image").performClick()
            onNodeWithTag("save-wish").performClick()
            onNode(hasText("Saved edit") and hasAnyAncestor(isDialog())).assertExists()
            runOnIdle {
                assertEquals(13, repository.space.value.active.size)
                assertNull(repository.space.value.active.first().content.cover)
                assertEquals("Saved edit", repository.space.value.active.first().content.title)
            }
            onNodeWithText("Back").performClick()
            onNodeWithText("Add a wish").performClick()
            onNodeWithTag("wish-title").performTextReplacement("Discard new wish")
            onNodeWithText("Cancel").performClick()
            runOnIdle { assertEquals(13, repository.space.value.active.size) }
        }

    @Test
    fun emptyFormAndValidation() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        WishForm(null, null, DemoSpace.create().couple, DemoDateSource.today(), {}, {})
                    }
                }
            }
            onNodeWithTag("save-wish").assertIsNotEnabled()
            onRoot().captureRoboImage("src/jvmTest/screenshots/form_empty_412.png")
            onNodeWithTag("wish-title").performTextReplacement("x".repeat(81))
            onNodeWithTag("save-wish").assertIsNotEnabled()
            onNodeWithText("81 / 80").assertExists()
            onNodeWithTag("wish-title").performTextReplacement("A new idea")
            onNodeWithTag("save-wish").assertIsEnabled()
        }

    @Test
    fun lightForm() = captureForm(false, 412)

    @Test
    fun darkForm() = captureForm(true, 412)

    @Test
    fun narrowForm() = captureForm(false, 320)

    private fun captureForm(dark: Boolean, width: Int) =
        runDesktopComposeUiTest(width = width, height = 917) {
            val initial =
                WishDraft(
                    "A weekend by the sea",
                    WishCategory.ADVENTURES,
                    "Find a little place by the coast. Slow mornings, long walks, and nowhere we need to be.",
                    WishDates.NoDate,
                    WishImage(WishCategory.ADVENTURES),
                    listOf(WishImage(WishCategory.ADVENTURES), WishImage(WishCategory.PLANS)),
                )
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = dark) {
                        WishForm("example", initial, DemoSpace.create().couple, DemoDateSource.today(), {}, {})
                    }
                }
            }
            val prefix = "src/jvmTest/screenshots/form_${if (dark) "dark" else "light"}_$width"
            onRoot().captureRoboImage("$prefix.png")
            onNodeWithText("Date range").performClick()
            onRoot().captureRoboImage("${prefix}_dates.png")
            onNodeWithTag("wish-form").performScrollToNode(hasText("Mia will see it in your shared list."))
            onNodeWithTag("save-wish").assertIsDisplayed()
            onRoot().captureRoboImage("${prefix}_images.png")
            onNodeWithTag("wish-form").performScrollToNode(hasTestTag("wish-description"))
            onNodeWithTag("wish-description").performTextReplacement("Long details. ".repeat(40))
            onNodeWithTag("save-wish").assertIsNotEnabled()
        }
}
