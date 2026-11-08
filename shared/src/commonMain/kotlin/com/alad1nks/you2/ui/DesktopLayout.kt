package com.alad1nks.you2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.brand
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import org.jetbrains.compose.resources.stringResource

internal val LocalDesktopLayout = staticCompositionLocalOf { false }

/** Keep the full window breakpoint separate from the workspace width after the rail. */
@Composable
internal fun NavigationWorkspace(selected: MainTab, onSelect: (MainTab) -> Unit, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val desktop = maxWidth >= 1200.dp
        CompositionLocalProvider(
            LocalDesktopLayout provides desktop,
            LocalMinimumInteractiveComponentSize provides
                if (desktop) 0.dp else LocalMinimumInteractiveComponentSize.current,
        ) {
            Row(Modifier.fillMaxSize()) {
                if (desktop) DesktopNavigation(selected, onSelect)
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        }
    }
}

@Composable
private fun DesktopNavigation(selected: MainTab, onSelect: (MainTab) -> Unit) {
    Column(
        Modifier.width(104.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainer)
            .testTag("desktop-navigation").padding(horizontal = 12.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        Row(
            Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcons.FavoriteBorder, null, Modifier.size(24.dp), MaterialTheme.colorScheme.primary)
            Text(
                stringResource(Res.string.brand),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MainTab.entries.forEach { tab ->
                val active = selected == tab
                val tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                Column(
                    Modifier.width(
                        80.dp,
                    ).height(72.dp).selectable(active, role = Role.Tab, onClick = { onSelect(tab) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        Modifier.size(64.dp, 32.dp).background(
                            if (active) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            },
                            RoundedCornerShape(20.dp),
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            when (tab) {
                                MainTab.WISHES -> AppIcons.FavoriteBorder
                                MainTab.MEMORIES -> AppIcons.PhotoLibrary
                                MainTab.US -> AppIcons.People
                            },
                            null,
                            Modifier.size(24.dp),
                            tint,
                        )
                    }
                    Text(
                        stringResource(
                            when (tab) {
                                MainTab.WISHES -> Res.string.wishes
                                MainTab.MEMORIES -> Res.string.memories
                                MainTab.US -> Res.string.us
                            },
                        ),
                        color = tint,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

internal fun Modifier.workspaceAction(desktop: Boolean, inset: androidx.compose.ui.unit.Dp): Modifier =
    if (desktop) {
        padding(start = 40.dp, end = 40.dp, top = 14.dp, bottom = 14.dp).width(240.dp).height(48.dp)
    } else {
        padding(horizontal = inset, vertical = 12.dp)
    }
