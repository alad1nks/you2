package com.alad1nks.you2.ui

import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.Completion
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoriesGroupingTest {
    @Test
    fun completionDateControlsYearMonthAndOrderRatherThanCreationDate() {
        val space = DemoSpace.create()
        val dates =
            listOf(LocalDate(2025, 12, 31), LocalDate(2026, 1, 2), LocalDate(2026, 1, 30), LocalDate(2025, 1, 1))
        val memories =
            space.memories.mapIndexed {
                index,
                wish,
                ->
                wish.copy(completion = Completion(dates[index], "alex"))
            }
        val groups = space.copy(wishes = space.active + memories).memoryGroups()
        assertEquals(listOf(LocalDate(2026, 1, 1), LocalDate(2025, 12, 1), LocalDate(2025, 1, 1)), groups.keys.toList())
        assertEquals(listOf(memories[2], memories[1]), groups.values.first())
        assertTrue(space.copy(wishes = space.active).memoryGroups().isEmpty())
    }
}
