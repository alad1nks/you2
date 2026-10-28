package com.alad1nks.you2.ui

import androidx.lifecycle.ViewModel
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.domain.GetDarkTheme
import com.alad1nks.you2.core.domain.GetLanguage
import com.alad1nks.you2.core.model.DateSource

internal class MainViewModel(
    getDarkTheme: GetDarkTheme,
    getLanguage: GetLanguage,
    repository: WishesRepository,
    dateSource: DateSource,
) : ViewModel() {
    val darkTheme = getDarkTheme()
    val language = getLanguage()
    val space = repository.space
    val today = dateSource.today()
}
