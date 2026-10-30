package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.feature.designsystem.components.DeleteWishDialog
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.add_wish
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.delete_action
import com.alad1nks.you2.resources.generated.delete_wish
import com.alad1nks.you2.resources.generated.edit_wish
import com.alad1nks.you2.resources.generated.mark_done
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.more_options
import com.alad1nks.you2.resources.generated.restore
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import com.alad1nks.you2.resources.generated.wishes_to_try
import io.github.takahirom.roborazzi.captureRoboImage
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PairedJourneyTest {
    @Test
    fun phoneUsesOnlyThePageInsets() = assertContentInsets(448, 24f, 424f)

    @Test
    fun wideWindowAlsoUsesOnlyThePageInsets() = assertContentInsets(800, 24f, 776f)

    private fun assertContentInsets(width: Int, left: Float, right: Float) =
        runDesktopComposeUiTest(width = width, height = 917) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(false) { AppContent(repository.space.value, DemoDateSource.today()) }
                }
            }
            val bounds = onNodeWithTag("paired-list").fetchSemanticsNode().boundsInRoot
            assertEquals(left, bounds.left)
            assertEquals(right, bounds.right)
        }

    @Test
    fun englishJourney() = journey("en", false, 412, 917, 1f)

    @Test
    fun russianNarrowLargeTextJourney() = journey("ru", false, 320, 640, 1.3f)

    @Test
    fun kazakhNarrowDarkLargeTextJourney() = journey("kk", true, 320, 640, 1.3f)

    @Test
    fun shortViewportKeepsFormAndLongConfirmationActionsReachable() =
        runDesktopComposeUiTest(width = 320, height = 360) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            var saved: WishDraft? = null
            var confirming by mutableStateOf(false)
            var deleted = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f), LocalAppLocale provides "ru") {
                    AppTheme(false) {
                        WishForm(null, null, repository.space.value.couple, DemoDateSource.today(), {}, {
                            saved = it
                            confirming = true
                        })
                        if (confirming) {
                            DeleteWishDialog(saved!!.title, "Mia", { confirming = false }, { deleted = true })
                        }
                    }
                }
            }
            val title = "A long shared wish with a title that takes several lines on a narrow screen"
            onNodeWithTag("wish-form").performScrollToNode(hasTestTag("wish-title"))
            onNodeWithTag("wish-title").performTextReplacement(title)
            onNodeWithTag("wish-form").performScrollToNode(hasTestTag("wish-description"))
            val description = "A memory to share. ".repeat(25)
            onNodeWithTag("wish-description").performTextReplacement(description)
            onNodeWithTag("save-wish").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/journey_short_form.png")
            onNodeWithTag("save-wish").performClick()
            runOnIdle {
                assertEquals(title, saved!!.title)
                assertEquals(description, saved!!.description)
            }
            onNodeWithText("Удалить").performScrollTo().assertIsDisplayed()
            onAllNodes(isRoot()).onLast().captureRoboImage("src/jvmTest/screenshots/journey_short_delete.png")
            onNodeWithText("Удалить").performClick()
            runOnIdle { assertTrue(deleted) }
        }

    private fun journey(locale: String, dark: Boolean, width: Int, height: Int, fontScale: Float) =
        runDesktopComposeUiTest(width = width, height = height) {
            val repository = InMemoryWishesRepository(DemoDateSource)
            val resources =
                listOf(
                    Res.string.add_wish,
                    Res.string.cancel,
                    Res.string.edit_wish,
                    Res.string.mark_done,
                    Res.string.memories,
                    Res.string.more_options,
                    Res.string.restore,
                    Res.string.wishes,
                    Res.string.delete_wish,
                    Res.string.delete_action,
                    Res.string.us,
                    Res.string.wishes_to_try,
                )
            val labels = mutableMapOf<org.jetbrains.compose.resources.StringResource, String>()
            setContent {
                val space by repository.space.collectAsState()
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale), LocalAppLocale provides locale) {
                    resources.forEach { labels[it] = stringResource(it) }
                    AppTheme(dark) {
                        AppContent(
                            space,
                            DemoDateSource.today(),
                            onDeleteWish = repository::delete,
                            onCompleteWish = repository::complete,
                            onRestoreWish = repository::restore,
                            onSaveWish = { id, draft ->
                                if (id == null) repository.create(draft) else repository.edit(id, draft)
                            },
                        )
                    }
                }
            }
            waitForIdle()
            val prefix = "src/jvmTest/screenshots/journey_${locale}_$width"

            fun snapshot(name: String) = onAllNodes(isRoot()).onLast().captureRoboImage("${prefix}_$name.png")

            fun click(resource: org.jetbrains.compose.resources.StringResource) =
                onNode(hasText(labels.getValue(resource)) and hasClickAction()).performClick()

            fun menu() = onNodeWithContentDescription(labels.getValue(Res.string.more_options)).performClick()

            fun counters(active: Int, memories: Int) {
                click(Res.string.us)
                onNodeWithTag("paired-list").performScrollToNode(hasText(labels.getValue(Res.string.wishes_to_try)))
                onNode(hasText(active.toString()) and hasAnyAncestor(hasTestTag("us-wishes"))).assertExists()
                onNode(hasText(memories.toString()) and hasAnyAncestor(hasTestTag("us-memories"))).assertExists()
            }
            onRoot().captureRoboImage("${prefix}_wishes.png")
            click(Res.string.add_wish)
            snapshot("form")
            onNodeWithTag("wish-title").performTextReplacement("An idea to discard")
            click(Res.string.cancel)
            runOnIdle { assertEquals(16, repository.space.value.wishes.size) }
            click(Res.string.add_wish)
            onNodeWithTag("wish-title").performTextReplacement("A shared weekend")
            onNodeWithTag("save-wish").assertIsDisplayed().performClick()
            val id = repository.space.value.active.first().id
            counters(13, 4)
            click(Res.string.wishes)
            onNodeWithTag("paired-list").performScrollToNode(hasText("A shared weekend"))
            onNodeWithText("A shared weekend").performClick()
            menu()
            click(Res.string.edit_wish)
            val title = "A shared weekend with long walks and time together away from everyday routines"
            onNodeWithTag("wish-title").performTextReplacement(title)
            onNodeWithTag("save-wish").performClick()
            snapshot("details")
            runOnIdle { assertEquals(title, repository.space.value.active.first { it.id == id }.content.title) }
            click(Res.string.mark_done)
            counters(12, 5)
            click(Res.string.memories)
            snapshot("memories")
            onNodeWithTag("memory-$id").performClick()
            snapshot("completed")
            runOnIdle {
                val completion = repository.space.value.memories.first { it.id == id }.completion!!
                assertEquals(DemoDateSource.today(), completion.date)
                assertEquals("alex", completion.authorId)
            }
            menu()
            snapshot("menu")
            click(Res.string.restore)
            onNodeWithTag("memory-$id").assertDoesNotExist()
            counters(13, 4)
            click(Res.string.wishes)
            onNodeWithTag("paired-list").performScrollToNode(hasText(title))
            onNodeWithText(title).performClick()
            runOnIdle { assertNull(repository.space.value.active.first { it.id == id }.completion) }
            menu()
            click(Res.string.delete_wish)
            snapshot("delete")
            click(Res.string.cancel)
            runOnIdle { assertEquals(17, repository.space.value.wishes.size) }
            menu()
            click(Res.string.delete_wish)
            click(Res.string.delete_action)
            onNodeWithText(title).assertDoesNotExist()
            counters(12, 4)
            snapshot("us")
            runOnIdle { assertEquals(16, repository.space.value.wishes.size) }
        }
}
