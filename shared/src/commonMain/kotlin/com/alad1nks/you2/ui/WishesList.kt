package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.feature.designsystem.components.CategoryChip
import com.alad1nks.you2.feature.designsystem.components.ContentCard
import com.alad1nks.you2.feature.designsystem.components.WishCard
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.all_wishes
import com.alad1nks.you2.resources.generated.empty_wishes
import com.alad1nks.you2.resources.generated.empty_wishes_hint
import com.alad1nks.you2.resources.generated.icon_heart
import com.alad1nks.you2.resources.generated.newest_first
import com.alad1nks.you2.resources.generated.no_filter_results
import com.alad1nks.you2.resources.generated.our_wishes
import com.alad1nks.you2.resources.generated.show_all_wishes
import com.alad1nks.you2.resources.generated.subtitle
import com.alad1nks.you2.resources.generated.summary_counts
import com.alad1nks.you2.resources.generated.summary_title
import com.alad1nks.you2.resources.generated.waiting_to_happen
import com.alad1nks.you2.resources.generated.wish_attribution
import com.alad1nks.you2.resources.generated.wish_attribution_you
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Stable sorting preserves the repository's newest-first insertion order within a day. */
internal fun PairedSpace.visibleWishes(category: WishCategory?) =
    active.filter { category == null || it.content.category == category }.sortedByDescending { it.createdOn }

internal fun LazyListScope.wishesContent(
    space: PairedSpace,
    today: LocalDate,
    category: WishCategory?,
    onCategory: (WishCategory?) -> Unit,
    onOpen: (String) -> Unit,
) {
    item(key = "heading") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(stringResource(Res.string.our_wishes), style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(Res.string.subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
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
                        painterResource(Res.drawable.icon_heart),
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            CategoryChip(stringResource(Res.string.all_wishes), category == null, { onCategory(null) })
            WishCategory.entries.forEach { option ->
                CategoryChip(stringResource(option.label), category == option, { onCategory(option) })
            }
        }
    }
    item(key = "sort") { WishesSortControl() }
    val wishes = space.visibleWishes(category)
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
    items(wishes, key = { it.id }, contentType = { "wish" }) { wish ->
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
            date = { WishDateBadge(wish.content.dates, today) },
        )
    }
}

@Composable
private fun WishesSortControl() {
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
                Text(stringResource(Res.string.newest_first), style = MaterialTheme.typography.bodySmall)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.newest_first)) },
                    leadingIcon = { Text("✓") },
                    onClick = { expanded = false },
                )
            }
        }
    }
}
