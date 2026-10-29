package com.alad1nks.you2.ui

import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.model.DateStatus
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.status
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PairedFoundationTest {
    @Test
    fun wishesAreFilteredAndSortedIndependentlyOfRepositoryOrder() {
        val space = DemoSpace.create()
        val shuffled = space.copy(wishes = space.wishes.reversed())
        assertEquals(space.active, shuffled.visibleWishes(null))
        WishCategory.entries.forEach { category ->
            assertEquals(space.active.filter { it.content.category == category }, shuffled.visibleWishes(category))
        }
        assertTrue(space.copy(wishes = space.memories).visibleWishes(null).isEmpty())
        assertTrue(
            space.copy(wishes = space.active.filterNot { it.content.category == WishCategory.MOVIES })
                .visibleWishes(WishCategory.MOVIES).isEmpty(),
        )
        val repository = InMemoryWishesRepository(DemoDateSource)
        repository.create(WishDraft("First today", WishCategory.OTHER))
        val newest = repository.create(WishDraft("Second today", WishCategory.OTHER))
        assertEquals(newest, repository.space.value.visibleWishes(null).first().id)
    }

    @Test
    fun demoCoversAllStatesAndBothAuthors() {
        val space = DemoSpace.create()
        assertEquals(12, space.active.size)
        assertEquals(4, space.memories.size)
        assertEquals(16, space.wishes.map { it.id }.toSet().size)
        assertEquals(WishCategory.entries.toSet(), space.active.map { it.content.category }.toSet())
        assertEquals(
            DateStatus.entries.toSet(),
            space.active.map { it.content.dates.status(DemoDateSource.today()) }.toSet(),
        )
        assertEquals(setOf("alex", "mia"), space.active.map { it.authorId }.toSet())
        assertEquals(listOf(2, 2), space.memories.groupBy { it.completion!!.date.month }.values.map { it.size })
    }

    @Test
    fun dateBoundariesAreInclusiveAndNeverCompleteWishes() {
        val today = DemoDateSource.today()
        assertEquals(DateStatus.TODAY, WishDates.Deadline(today).status(today))
        assertEquals(DateStatus.OVERDUE, WishDates.Deadline(LocalDate(2026, 10, 27)).status(today))
        assertEquals(DateStatus.UPCOMING_DEADLINE, WishDates.Deadline(LocalDate(2026, 10, 29)).status(today))
        assertEquals(DateStatus.IN_PROGRESS, WishDates.Range(today, today).status(today))
        assertEquals(DateStatus.IN_PROGRESS, WishDates.Range(LocalDate(2026, 10, 27), today).status(today))
        assertEquals(DateStatus.IN_PROGRESS, WishDates.Range(today, LocalDate(2026, 10, 29)).status(today))
        val range = WishDates.Range(LocalDate(2028, 2, 29), LocalDate(2028, 3, 1))
        assertEquals(DateStatus.UPCOMING_RANGE, range.status(LocalDate(2028, 2, 28)))
        assertEquals(DateStatus.IN_PROGRESS, range.status(LocalDate(2028, 2, 29)))
        assertEquals(DateStatus.IN_PROGRESS, range.status(LocalDate(2028, 3, 1)))
        assertEquals(DateStatus.ENDED, range.status(LocalDate(2028, 3, 2)))
        assertFailsWith<IllegalArgumentException> { WishDates.Range(LocalDate(2026, 10, 29), today) }
        assertEquals(12, InMemoryWishesRepository(DemoDateSource).space.value.active.size)
    }

    @Test
    fun changesShareOneStateAndRestartResetsIt() {
        val repository = InMemoryWishesRepository(DemoDateSource)
        val observed = repository.space
        val draft = WishDraft("A new idea", WishCategory.OTHER)
        val id = repository.create(draft)
        assertEquals(13, observed.value.active.size)
        repository.edit(id, draft.copy(title = "An edited idea"))
        repository.complete(id)
        val completed = observed.value.memories.single { it.id == id }
        assertEquals("An edited idea", completed.content.title)
        assertEquals(DemoDateSource.today(), completed.completion!!.date)
        assertEquals("alex", completed.completion!!.authorId)
        assertEquals("", completed.completion!!.memory)
        repository.editMemory(id, "A lovely day")
        repository.complete(id)
        assertEquals("A lovely day", observed.value.memories.single { it.id == id }.completion!!.memory)
        repository.restore(id)
        assertNull(observed.value.active.single { it.id == id }.completion)
        repository.delete(id)
        assertEquals(12, observed.value.active.size)
        assertEquals(4, observed.value.memories.size)
        assertEquals(DemoSpace.create(), InMemoryWishesRepository(DemoDateSource).space.value)
    }

    @Test
    fun invalidDraftDoesNotMutateRepository() {
        val repository = InMemoryWishesRepository(DemoDateSource)
        val initial = repository.space.value
        listOf("", "   ", "a".repeat(81)).forEach { title ->
            assertFailsWith<IllegalArgumentException> { repository.create(WishDraft(title, WishCategory.OTHER)) }
        }
        assertFalse(WishDraft("Valid", WishCategory.OTHER, "a".repeat(501)).isValid)
        assertTrue(WishDraft("a".repeat(80), WishCategory.OTHER, "a".repeat(500)).isValid)
        assertEquals(initial, repository.space.value)
        val first = repository.create(WishDraft("First", WishCategory.OTHER))
        val second = repository.create(WishDraft("Second", WishCategory.OTHER))
        assertNotEquals(first, second)
        repository.delete(second)
        assertNotEquals(second, repository.create(WishDraft("Third", WishCategory.OTHER)))
    }

    @Test
    fun returningFromAnOverlayPreservesTabAndFilter() {
        val navigation = PairedNavigation()
        navigation.category = WishCategory.MOVIES
        navigation.open(OverlayRoute.Details("wish-1"))
        navigation.back()
        assertEquals(MainTab.WISHES, navigation.tab)
        assertEquals(WishCategory.MOVIES, navigation.category)
        navigation.open(OverlayRoute.Details("wish-1"))
        navigation.open(OverlayRoute.Form("wish-1"))
        navigation.back()
        assertEquals(OverlayRoute.Details("wish-1"), navigation.overlay)
        navigation.back()
        assertNull(navigation.overlay)
        navigation.select(MainTab.MEMORIES)
        navigation.open(OverlayRoute.Details("wish-2"))
        navigation.back()
        assertEquals(MainTab.MEMORIES, navigation.tab)
        assertNull(navigation.overlay)
    }
}
