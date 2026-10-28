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
import kotlin.test.assertTrue

class WishFormStateTest {
    @Test
    fun requiredTitleAndLengthBoundaries() {
        val form = WishFormState()
        assertNull(form.draftOrNull())
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
        form.titleTouched = true
        val saved = with(WishFormState.Saver) { SaverScope { true }.save(form) }
        val restored = WishFormState.Saver.restore(requireNotNull(saved))!!
        assertEquals("Edited", restored.title)
        assertEquals(form.attachments, restored.attachments)
        assertNull(restored.cover)
        assertEquals(form.start, restored.start)
        assertNull(restored.end)
        assertTrue(restored.titleTouched)
        assertNull(restored.draftOrNull())
        assertEquals(2, original.attachments.size)
        assertNotNull(original.cover)
    }
}
