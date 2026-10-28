package com.alad1nks.you2.core.data.repository

import com.alad1nks.you2.core.model.Completion
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.Wish
import com.alad1nks.you2.core.model.WishDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

class InMemoryWishesRepository(
    private val dateSource: DateSource,
    initial: PairedSpace = DemoSpace.create(),
) : WishesRepository {
    private val state = MutableStateFlow(initial)
    private val sequence =
        MutableStateFlow(
            initial.wishes.mapNotNull { it.id.removePrefix("wish-").toLongOrNull() }.maxOrNull() ?: 0,
        )
    override val space = state.asStateFlow()

    override fun create(draft: WishDraft): String {
        require(draft.isValid)
        val id = "wish-${sequence.updateAndGet { it + 1 }}"
        val wish =
            Wish(
                id,
                state.value.couple.you.id,
                dateSource.today(),
                draft.copy(attachments = draft.attachments.toList()),
            )
        state.update { it.copy(wishes = listOf(wish) + it.wishes) }
        return id
    }

    override fun edit(id: String, draft: WishDraft) {
        require(draft.isValid)
        change(id) { it.copy(content = draft.copy(attachments = draft.attachments.toList())) }
    }

    override fun complete(id: String) =
        change(id) {
            if (it.completion != null) {
                it
            } else {
                it.copy(
                    completion = Completion(dateSource.today(), state.value.couple.you.id),
                )
            }
        }

    override fun restore(id: String) = change(id) { it.copy(completion = null) }

    override fun editMemory(id: String, memory: String) {
        require(memory.length <= 500)
        change(id) { it.copy(completion = requireNotNull(it.completion).copy(memory = memory)) }
    }

    override fun delete(id: String) {
        state.update { it.copy(wishes = it.wishes.filterNot { wish -> wish.id == id }) }
    }

    override fun renameUser(name: String) {
        require(name.isNotBlank())
        state.update { it.copy(couple = it.couple.copy(you = it.couple.you.copy(name = name.trim()))) }
    }

    private fun change(id: String, transform: (Wish) -> Wish) {
        state.update { current ->
            require(current.wishes.any { it.id == id })
            current.copy(wishes = current.wishes.map { if (it.id == id) transform(it) else it })
        }
    }
}
