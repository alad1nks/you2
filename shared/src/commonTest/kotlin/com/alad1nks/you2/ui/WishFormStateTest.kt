package com.alad1nks.you2.ui

import androidx.compose.runtime.saveable.SaverScope
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WishFormStateTest {
    @Test
    fun selectedAndUploadedImageReferencesSurviveFormRestoration() {
        val draft =
            WishDraft(
                "Photos",
                WishCategory.OTHER,
                cover = WishImage(source = "file:///cache/selected.jpg"),
                attachments = listOf(WishImage(source = "couples/pair/wishes/id/photo.jpg")),
            )
        val form = WishFormState(draft)
        val saved = with(WishFormState.Saver) { SaverScope { true }.save(form) }
        assertEquals(draft, WishFormState.Saver.restore(requireNotNull(saved))!!.draftOrNull())
    }

    @Test
    fun excessiveAttachmentsCannotReachTheRepository() {
        val form = WishFormState()
        form.title = "A wish"
        form.attachments = List(13) { WishImage(WishCategory.OTHER) }
        assertNull(form.draftOrNull())
        form.attachments = form.attachments.dropLast(1)
        assertNotNull(form.draftOrNull())
    }

    @Test
    fun requiredTitleAndLengthBoundaries() {
        val form = WishFormState()
        assertNull(form.draftOrNull())
        assertEquals(WishCategory.OTHER, form.category)
        assertEquals(WishDateMode.NONE, form.mode)
        form.title = "   "
        assertNull(form.draftOrNull())
        form.title = "a".repeat(80)
        form.description = "b".repeat(500)
        assertNotNull(form.draftOrNull())
        form.title += "a"
        assertNull(form.draftOrNull())
        form.title = "Valid"
        form.description += "b"
        assertNull(form.draftOrNull())
    }

    @Test
    fun missingReversedEqualAndClearedDates() {
        val form = WishFormState(WishDraft("An idea", WishCategory.OTHER))
        val today = DemoDateSource.today()
        form.selectMode(WishDateMode.DEADLINE)
        assertNull(form.draftOrNull())
        form.start = today
        assertEquals(WishDates.Deadline(today), form.draftOrNull()?.dates)
        form.selectMode(WishDateMode.RANGE)
        assertNull(form.draftOrNull())
        form.end = LocalDate(2026, 10, 27)
        assertNull(form.draftOrNull())
        form.end = today
        assertEquals(WishDates.Range(today, today), form.draftOrNull()?.dates)
        form.end = LocalDate(2027, 1, 1)
        assertNotNull(form.draftOrNull())
        form.selectMode(WishDateMode.NONE)
        assertNull(form.start)
        assertNull(form.end)
        assertEquals(WishDates.NoDate, form.draftOrNull()?.dates)
        form.selectMode(WishDateMode.RANGE)
        assertNull(form.draftOrNull())
    }

    @Test
    fun editsAreIsolatedAndSaveableIncludingIncompleteDatesAndImages() {
        val original =
            DemoSpace.create().active.first().content.copy(
                cover = WishImage(WishCategory.MOVIES),
                attachments = listOf(WishImage(WishCategory.PLANS), WishImage(WishCategory.PLANS)),
            )
        val form = WishFormState(original)
        assertEquals(original, form.draftOrNull())
        form.title = "Edited"
        form.cover = null
        form.attachments = form.attachments.drop(1)
        form.selectMode(WishDateMode.RANGE)
        form.start = DemoDateSource.today()
        val saved = with(WishFormState.Saver) { SaverScope { true }.save(form) }
        val restored = WishFormState.Saver.restore(requireNotNull(saved))!!
        assertEquals("Edited", restored.title)
        assertEquals(form.attachments, restored.attachments)
        assertNull(restored.cover)
        assertEquals(form.start, restored.start)
        assertNull(restored.end)
        assertNull(restored.draftOrNull())
        assertEquals(2, original.attachments.size)
        assertNotNull(original.cover)
    }
}
