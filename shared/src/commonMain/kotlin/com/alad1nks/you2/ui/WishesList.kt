package com.alad1nks.you2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.CategoryChip
import com.alad1nks.you2.feature.designsystem.components.ContentCard
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.components.WishCard
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.add_wish
import com.alad1nks.you2.resources.generated.all_wishes
import com.alad1nks.you2.resources.generated.empty_wishes
import com.alad1nks.you2.resources.generated.empty_wishes_hint
import com.alad1nks.you2.resources.generated.newest_first
import com.alad1nks.you2.resources.generated.no_filter_results
import com.alad1nks.you2.resources.generated.oldest_first
import com.alad1nks.you2.resources.generated.our_wishes
import com.alad1nks.you2.resources.generated.show_all_wishes
import com.alad1nks.you2.resources.generated.soonest_date
import com.alad1nks.you2.resources.generated.subtitle
import com.alad1nks.you2.resources.generated.summary_counts
import com.alad1nks.you2.resources.generated.summary_title
import com.alad1nks.you2.resources.generated.waiting_to_happen
import com.alad1nks.you2.resources.generated.wish_attribution
import com.alad1nks.you2.resources.generated.wish_attribution_you
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.wishesContent(
    space: PairedSpace,
    today: LocalDate,
    category: WishCategory?,
    onCategory: (WishCategory?) -> Unit,
    onOpen: (String) -> Unit,
    sort: WishSort,
    onSort: (WishSort) -> Unit,
    columns: Int = 1,
    onAdd: (() -> Unit)? = null,
) {
    item(key = "heading") {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(stringResource(Res.string.our_wishes), style = MaterialTheme.typography.headlineLarge)
                Text(
                    stringResource(Res.string.subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            if (onAdd != null) {
                PrimaryButton(
                    stringResource(
                        Res.string.add_wish,
                    ),
                    onAdd,
                    Modifier.width(180.dp),
                    leadingIcon = {
                        Text("+")
                    },
                )
            }
        }
    }
    item(key = "summary") {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(AppSpacing.large),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background) {
                    Icon(
                        AppIcons.FavoriteBorder,
                        null,
                        Modifier.padding(AppSpacing.listGap).size(AppDimensions.icon),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(stringResource(Res.string.summary_title), style = MaterialTheme.typography.labelLarge)
                    Text(
                        stringResource(Res.string.summary_counts, space.active.size, space.memories.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
    item(key = "filters") {
        val options = listOf<WishCategory?>(null) + WishCategory.entries
        val rows = if (columns == 2) listOf(options.take(3), options.drop(3)) else listOf(options)
        Column {
            rows.forEach { row ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    row.forEach { option ->
                        CategoryChip(
                            stringResource(option?.label ?: Res.string.all_wishes),
                            category == option,
                            { onCategory(option) },
                        )
                    }
                }
            }
        }
    }
    item(key = "sort") { WishesSortControl(sort, onSort) }
    val wishes = space.visibleWishes(category, sort)
    if (wishes.isEmpty()) {
        item(key = "empty") {
            ContentCard {
                Text(
                    stringResource(
                        if (space.active.isEmpty()) Res.string.empty_wishes else Res.string.no_filter_results,
                    ),
                )
                if (space.active.isEmpty()) {
                    Text(stringResource(Res.string.empty_wishes_hint), style = MaterialTheme.typography.bodySmall)
                } else {
                    TextButton({ onCategory(null) }) { Text(stringResource(Res.string.show_all_wishes)) }
                }
            }
        }
    }
    items(wishes.chunked(columns), key = { it.first().id }, contentType = { "wish-row-$columns" }) { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            row.forEach { wish ->
                Box(Modifier.weight(1f)) {
                    val categoryLabel = stringResource(wish.content.category.label).uppercase()
                    WishCard(
                        title = wish.content.title,
                        description = wish.content.description,
                        author =
                            if (wish.authorId == space.couple.you.id) {
                                stringResource(Res.string.wish_attribution_you, categoryLabel)
                            } else {
                                stringResource(Res.string.wish_attribution, categoryLabel, space.couple.partner.name)
                            },
                        onClick = { onOpen(wish.id) },
                        modifier = Modifier.testTag(wish.id),
                        contentMinHeight = if (columns > 1) 116.dp else 0.dp,
                        date = { WishDateBadge(wish.content.dates, today) },
                    )
                }
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
internal fun WishesSortControl(sort: WishSort, onSort: (WishSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            stringResource(Res.string.waiting_to_happen),
            Modifier.align(Alignment.CenterVertically),
            style = MaterialTheme.typography.labelLarge,
        )
        Box {
            TextButton({ expanded = true }, Modifier.testTag("wish-sort")) {
                Text(
                    stringResource(sort.label) + if (expanded) " ↑" else " ↓",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            DropdownMenu(
                expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(220.dp).testTag("wish-sort-menu"),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                WishSort.entries.forEach { option ->
                    val isSelected = sort == option
                    val color =
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(option.label),
                                style = MaterialTheme.typography.labelLarge,
                                color = color,
                            )
                        },
                        trailingIcon = if (isSelected) ({ Text("✓", color = color) }) else null,
                        modifier =
                            Modifier.padding(horizontal = AppSpacing.small)
                                .semantics { selected = isSelected }
                                .testTag("wish-sort-${option.name}")
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(8.dp),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                        contentPadding = PaddingValues(horizontal = AppSpacing.medium),
                        onClick = {
                            onSort(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private val WishSort.label
    get() =
        when (this) {
            WishSort.NEWEST -> Res.string.newest_first
            WishSort.OLDEST -> Res.string.oldest_first
            WishSort.SOONEST -> Res.string.soonest_date
        }
