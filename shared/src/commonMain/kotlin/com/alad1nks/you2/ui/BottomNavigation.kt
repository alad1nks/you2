package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomNavigation(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
        MainTab.entries.forEach { tab ->
            val label =
                stringResource(
                    when (tab) {
                        MainTab.WISHES -> Res.string.wishes
                        MainTab.MEMORIES -> Res.string.memories
                        MainTab.US -> Res.string.us
                    },
                )
            NavigationBarItem(
                selected = selected == tab,
                colors =
                    NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                onClick = {
                    onSelect(tab)
                },
                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                icon = {
                    Icon(
                        when (tab) {
                            MainTab.WISHES -> AppIcons.FavoriteBorder
                            MainTab.MEMORIES -> AppIcons.PhotoLibrary
                            MainTab.US -> AppIcons.People
                        },
                        null,
                        Modifier.size(AppDimensions.icon),
                    )
                },
            )
        }
    }
}
