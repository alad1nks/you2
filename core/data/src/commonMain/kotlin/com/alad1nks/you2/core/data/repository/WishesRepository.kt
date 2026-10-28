package com.alad1nks.you2.core.data.repository

import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishDraft
import kotlinx.coroutines.flow.StateFlow

interface WishesRepository {
    val space: StateFlow<PairedSpace>

    fun create(draft: WishDraft): String

    fun edit(id: String, draft: WishDraft)

    fun complete(id: String)

    fun restore(id: String)

    fun editMemory(id: String, memory: String)

    fun delete(id: String)

    fun renameUser(name: String)
}
