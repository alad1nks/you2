package com.alad1nks.you2.ui

import androidx.lifecycle.ViewModel
import com.alad1nks.you2.core.domain.GetDarkTheme
import com.alad1nks.you2.core.domain.GetLanguage

internal class MainViewModel(
    getDarkTheme: GetDarkTheme,
    getLanguage: GetLanguage,
) : ViewModel() {
    val darkTheme = getDarkTheme()
    val language = getLanguage()
}
