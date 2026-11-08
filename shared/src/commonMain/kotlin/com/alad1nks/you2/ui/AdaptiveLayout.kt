package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Window width, rather than device type, also handles tablet split-screen windows. */
internal data class AppLayout(val width: Dp) {
    val tablet = width >= 600.dp
    val expanded = width >= 1000.dp
    val wishColumns =
        when {
            expanded -> 3
            width >= 760.dp -> 2
            else -> 1
        }
    val listInset = if (tablet) 40.dp else 24.dp
    val detailInset =
        when {
            expanded -> 64.dp
            tablet -> ((width - 642.dp) / 2).coerceAtLeast(24.dp)
            else -> 24.dp
        }
    val actionInset = if (expanded) ((width - 520.dp) / 2).coerceAtLeast(24.dp) else 24.dp
}

/** Shared section content keeps controls and state identical in stacked and split layouts. */
internal class AdaptiveSections {
    val sections = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        sections.add(content)
    }

    fun <T> itemsIndexed(items: List<T>, content: @Composable (Int, T) -> Unit) {
        items.forEachIndexed { index, item -> sections.add { content(index, item) } }
    }
}

internal fun LazyListScope.adaptiveSections(
    expanded: Boolean,
    splitAt: Int,
    heading: Boolean = false,
    gap: Dp = 20.dp,
    rightTop: Dp = 0.dp,
    content: AdaptiveSections.() -> Unit,
) {
    val sections = AdaptiveSections().apply(content).sections
    if (!expanded) {
        sections.forEach { section -> item { Column { section() } } }
    } else {
        if (heading) item { Column { sections.first()() } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                    sections.subList(if (heading) 1 else 0, splitAt).forEach { section -> Column { section() } }
                }
                Column(
                    Modifier.weight(1f).padding(top = rightTop),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    sections.drop(splitAt).forEach { section -> Column { section() } }
                }
            }
        }
    }
}

@Composable
internal fun AdaptiveContent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    splitAt: Int,
    heading: Boolean = false,
    gap: Dp = 20.dp,
    content: AdaptiveSections.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val layout = AppLayout(maxWidth)
        LazyColumn(
            modifier.fillMaxSize(),
            state = state,
            contentPadding =
                PaddingValues(
                    horizontal = layout.detailInset,
                    vertical =
                        if (heading) {
                            16.dp
                        } else if (layout.tablet) {
                            32.dp
                        } else {
                            16.dp
                        },
                ),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            adaptiveSections(
                layout.expanded,
                splitAt,
                heading,
                if (heading && layout.expanded) 16.dp else gap,
                content = content,
            )
        }
    }
}
