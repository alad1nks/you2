package com.alad1nks.you2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.domain.GetDarkTheme
import com.alad1nks.you2.core.domain.GetLanguage
import com.alad1nks.you2.core.domain.SetDarkTheme
import com.alad1nks.you2.core.domain.SetLanguage
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.core.model.WishDraft
import kotlinx.coroutines.launch

internal class MainViewModel(
    getDarkTheme: GetDarkTheme,
    getLanguage: GetLanguage,
    private val repository: WishesRepository,
    dateSource: DateSource,
    private val setDarkTheme: SetDarkTheme,
    private val setLanguage: SetLanguage,
) : ViewModel() {
    val darkTheme = getDarkTheme()
    val language = getLanguage()
    val space = repository.space
    val cloud = repository as? CloudWishesRepository
    val today = dateSource.today()

    fun renameUser(name: String) = repository.renameUser(name)

    fun changeDarkTheme(value: Boolean) = viewModelScope.launch { setDarkTheme(value) }

    fun changeLanguage(value: Language) = viewModelScope.launch { setLanguage(value) }

    fun saveWish(id: String?, draft: WishDraft) {
        if (id == null) repository.create(draft) else repository.edit(id, draft)
    }

    fun deleteWish(id: String) = repository.delete(id)

    fun restoreWish(id: String) = repository.restore(id)

    fun editMemory(id: String, memory: String) = repository.editMemory(id, memory)

    fun completeWish(id: String) = repository.complete(id)
}
