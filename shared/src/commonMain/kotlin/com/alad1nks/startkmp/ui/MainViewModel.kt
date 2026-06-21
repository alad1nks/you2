package com.alad1nks.startkmp.ui

import androidx.lifecycle.ViewModel
import com.alad1nks.startkmp.core.domain.GetDarkTheme
import com.alad1nks.startkmp.core.domain.GetLanguage

internal class MainViewModel(
    getDarkTheme: GetDarkTheme,
    getLanguage: GetLanguage,
) : ViewModel() {
    val darkTheme = getDarkTheme()
    val language = getLanguage()
}
