package com.alad1nks.you2.ui

import androidx.compose.runtime.saveable.SaverScope
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.Completion
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WishSortTest {
    @Test
    fun sortsDatesWithUndatedLastAndExcludesCompletedWishes() {
        val initial = DemoSpace.create()
        val base = initial.active.first()
        val day = LocalDate(2026, 10, 28)
        val early = LocalDate(2026, 10, 20)
        val late = LocalDate(2026, 11, 1)
        val space =
            initial.copy(
                wishes =
                    listOf(
                        base.copy(
                            id = "undated",
                            createdOn = late,
                            content = base.content.copy(dates = WishDates.NoDate),
                        ),
                        base.copy(
                            id = "deadline",
                            createdOn = day,
                            content = base.content.copy(dates = WishDates.Deadline(day)),
                        ),
                        base.copy(
                            id = "range",
                            createdOn = early,
                            content = base.content.copy(dates = WishDates.Range(early, late)),
                        ),
                        base.copy(
                            id = "tie",
                            createdOn = day,
                            content = base.content.copy(dates = WishDates.Deadline(day)),
                        ),
                        base.copy(id = "completed", completion = Completion(day, base.authorId)),
                    ),
            )
        assertEquals(
            listOf("range", "deadline", "tie", "undated"),
            space.visibleWishes(null, WishSort.SOONEST).map { it.id },
        )
        assertEquals(
            listOf("undated", "deadline", "tie", "range"),
            space.visibleWishes(null, WishSort.NEWEST).map { it.id },
        )
        assertEquals(
            listOf("range", "deadline", "tie", "undated"),
            space.visibleWishes(null, WishSort.OLDEST).map { it.id },
        )
        WishSort.entries.forEach { sort ->
            assertEquals(emptyList(), space.visibleWishes(WishCategory.OTHER, sort))
            assertEquals(space.visibleWishes(null, sort), space.visibleWishes(base.content.category, sort))
        }
    }

    @Test
    fun restoresCategoryAndSortWithNavigation() {
        val navigation =
            PairedNavigation().apply {
                category = WishCategory.MOVIES
                sort = WishSort.SOONEST
                select(MainTab.MEMORIES)
            }
        val saved = with(PairedNavigation.Saver) { SaverScope { true }.save(navigation) }
        val restored = PairedNavigation.Saver.restore(requireNotNull(saved))!!
        restored.select(MainTab.WISHES)
        assertEquals(WishCategory.MOVIES, restored.category)
        assertEquals(WishSort.SOONEST, restored.sort)
    }
}
