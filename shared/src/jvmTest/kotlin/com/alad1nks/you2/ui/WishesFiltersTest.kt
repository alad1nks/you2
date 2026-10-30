package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class WishesFiltersTest {
    @Test
    fun sortWorksWithCategoryAndSurvivesNavigation() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) { AppContent(DemoSpace.create(), DemoDateSource.today()) }
                }
            }
            onNodeWithTag("wish-sort").performClick()
            onNodeWithTag("wish-sort-NEWEST").assertIsSelected()
            onNodeWithTag("wish-sort-menu").captureRoboImage("src/jvmTest/screenshots/wishes_sort_menu.png")
            onNodeWithTag("wish-sort-SOONEST").performClick()
            onNodeWithText("A slow Sunday together").assertIsDisplayed()
            onNodeWithText("Movies").performClick()
            onNodeWithText("Finally watch About Time").assertIsDisplayed()
            onNodeWithText("Memories").performClick()
            onNodeWithText("Wishes").performClick()
            onNodeWithText("Movies").assertIsSelected()
            onNodeWithTag("wish-sort").performClick()
            onNodeWithTag("wish-sort-SOONEST").assertIsSelected()
            onNodeWithTag("wish-sort-OLDEST").performClick()
            onNodeWithText("A classic movie marathon").assertIsDisplayed()
        }
}
