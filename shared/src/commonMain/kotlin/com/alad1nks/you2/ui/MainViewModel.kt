package com.alad1nks.you2.ui

import androidx.lifecycle.ViewModel
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.domain.GetDarkTheme
import com.alad1nks.you2.core.domain.GetLanguage
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.WishDraft

internal class MainViewModel(
    getDarkTheme: GetDarkTheme,
    getLanguage: GetLanguage,
    private val repository: WishesRepository,
    dateSource: DateSource,
) : ViewModel() {
    val darkTheme = getDarkTheme()
    val language = getLanguage()
    val space = repository.space
    val today = dateSource.today()

    fun saveWish(id: String?, draft: WishDraft) {
        if (id == null) repository.create(draft) else repository.edit(id, draft)
    }

    fun deleteWish(id: String) = repository.delete(id)

    fun completeWish(id: String) = repository.complete(id)
}
