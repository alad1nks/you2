package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
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
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class MemoriesTest {
    @Test
    fun completeEditCancelClearRestoreAndDelete() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            val wish = repository.space.value.active.first()
            setContent {
                val space by repository.space.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        AppContent(
                            space,
                            DemoDateSource.today(),
                            repository::delete,
                            repository::complete,
                            onRestoreWish = repository::restore,
                            onEditMemory = repository::editMemory,
                        )
                    }
                }
            }
            onNodeWithText(wish.content.title).performClick()
            onNodeWithText("Mark as done").performClick()
            onNodeWithText("Memories").performClick()
            onNodeWithTag("memory-${wish.id}").performClick()
            onNodeWithText("Completed on 28 Oct 2026").assertIsDisplayed()
            onNodeWithTag("wish-details").performScrollToNode(hasText("No memory added yet."))
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit memory").performClick()
            onNodeWithTag("memory-draft").performTextReplacement("Unsaved")
            onNodeWithText("Cancel").performClick()
            runOnIdle {
                assertEquals(
                    "",
                    repository.space.value.memories.first { it.id == wish.id }.completion!!.memory,
                )
            }
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit memory").performClick()
            onNodeWithTag("memory-draft").performTextReplacement("a".repeat(501))
            onNodeWithText("Save changes").assertIsNotEnabled()
            onNodeWithTag("memory-draft").performTextReplacement("a".repeat(500))
            onNodeWithText("Save changes").performClick()
            runOnIdle {
                val updated = repository.space.value.memories.first { it.id == wish.id }
                assertEquals(500, updated.completion!!.memory.length)
                assertEquals(wish.content, updated.content)
                assertEquals("alex", updated.completion!!.authorId)
            }
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Edit memory").performClick()
            onNodeWithTag("memory-draft").performTextReplacement("")
            onNodeWithText("Save changes").performClick()
            onNodeWithTag("wish-details").performScrollToNode(hasText("No memory added yet."))
            onNodeWithText("Back to Memories").performClick()
            onNodeWithTag("memory-${wish.id}").performClick()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Move back to Wishes").performClick()
            onNodeWithTag("memory-${wish.id}").assertDoesNotExist()
            runOnIdle {
                assertEquals(12, repository.space.value.active.size)
                assertEquals(4, repository.space.value.memories.size)
                assertNull(repository.space.value.active.first { it.id == wish.id }.completion)
            }
            onNodeWithText("Wishes").performClick()
            onNodeWithText(wish.content.title).performClick()
            onNodeWithText("Mark as done").performClick()
            onNodeWithText("Memories").performClick()
            onNodeWithTag("memory-${wish.id}").performClick()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Delete wish").performClick()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Back to Memories").assertIsDisplayed()
            onNodeWithContentDescription("More options").performClick()
            onNodeWithText("Delete wish").performClick()
            onNodeWithText("Delete").performClick()
            runOnIdle { assertEquals(15, repository.space.value.wishes.size) }
        }

    @Test
    fun lightMemories() = captureMemories(false, 412)

    @Test
    fun darkMemories() = captureMemories(true, 412)

    @Test
    fun narrowMemories() = captureMemories(false, 320)

    private fun captureMemories(dark: Boolean, width: Int) =
        runDesktopComposeUiTest(width = width, height = 917) {
            var space by mutableStateOf(DemoSpace.create())
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = dark) { AppContent(space, DemoDateSource.today()) }
                }
            }
            val prefix = "src/jvmTest/screenshots/memories_${if (dark) "dark" else "light"}_$width"
            onNodeWithText("Memories").performClick()
            onRoot().captureRoboImage("$prefix.png")
            val wish = space.memoryGroups().values.first().first()
            onNodeWithTag("memory-${wish.id}").performClick()
            onNodeWithTag("wish-details").assertIsDisplayed()
            onRoot().captureRoboImage("${prefix}_details.png")
            onNodeWithTag("wish-details").performScrollToNode(hasText("A little memory"))
            onRoot().captureRoboImage("${prefix}_details_end.png")
            onNodeWithContentDescription("More options").performClick()
            onAllNodes(isRoot()).onLast().captureRoboImage("${prefix}_menu.png")
            onNodeWithText("Edit memory").performClick()
            onAllNodes(isRoot()).onLast().captureRoboImage("${prefix}_editor.png")
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Back to Memories").performClick()
            onNodeWithTag("paired-list").performScrollToNode(hasText("September 2026"))
            val last = space.memoryGroups().values.last().last()
            onNodeWithTag("paired-list").performScrollToNode(hasText(last.content.title))
            onNodeWithTag("memory-${last.id}").performClick()
            onNodeWithText("Back to Memories").performClick()
            onNodeWithTag("memory-${last.id}").assertIsDisplayed()
            runOnIdle { space = space.copy(wishes = space.active) }
            onNodeWithText("Your stories are still ahead.").assertExists()
            onRoot().captureRoboImage("${prefix}_empty.png")
        }
}
