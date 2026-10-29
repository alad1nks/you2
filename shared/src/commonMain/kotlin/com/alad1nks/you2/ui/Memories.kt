package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.ContentCard
import com.alad1nks.you2.feature.designsystem.components.WishCard
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.empty_memories
import com.alad1nks.you2.resources.generated.empty_memories_hint
import com.alad1nks.you2.resources.generated.full_months
import com.alad1nks.you2.resources.generated.memories_subtitle
import com.alad1nks.you2.resources.generated.memories_summary
import com.alad1nks.you2.resources.generated.memories_summary_hint
import com.alad1nks.you2.resources.generated.memory_count
import com.alad1nks.you2.resources.generated.memory_metadata
import com.alad1nks.you2.resources.generated.memory_month
import com.alad1nks.you2.resources.generated.one_memory
import com.alad1nks.you2.resources.generated.our_memories
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Sorting before grouping retains chronological order across years and within each month. */
internal fun PairedSpace.memoryGroups() =
    memories.sortedByDescending { it.completion!!.date }.groupBy {
        val date = it.completion!!.date
        LocalDate(date.year, date.month, 1)
    }

internal fun LazyListScope.memoriesContent(space: PairedSpace, onOpen: (String) -> Unit) {
    item(key = "memories-heading") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(stringResource(Res.string.our_memories), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(Res.string.memories_subtitle), color = MaterialTheme.colorScheme.secondary)
        }
    }
    item(key = "memories-summary") {
        CompletionSummary(
            stringResource(Res.string.memories_summary, space.memories.size),
            stringResource(Res.string.memories_summary_hint),
        )
    }
    if (space.memories.isEmpty()) {
        item(key = "memories-empty") {
            ContentCard {
                Text(stringResource(Res.string.empty_memories), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(Res.string.empty_memories_hint))
            }
        }
    }
    space.memoryGroups().forEach { (month, wishes) ->
        item(key = "month-$month") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(
                    stringResource(
                        Res.string.memory_month,
                        stringArrayResource(Res.array.full_months)[month.month.ordinal],
                        month.year,
                    ),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    if (wishes.size == 1) {
                        stringResource(
                            Res.string.one_memory,
                        )
                    } else {
                        stringResource(Res.string.memory_count, wishes.size)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        items(wishes, key = { it.id }) { wish ->
            WishCard(
                wish.content.title,
                wish.completion!!.memory,
                stringResource(
                    Res.string.memory_metadata,
                    stringResource(wish.content.category.label),
                    wishDateText(wish.completion!!.date),
                ),
                onClick = { onOpen(wish.id) },
                modifier = Modifier.testTag("memory-${wish.id}"),
                symbol = AppIcons.CheckCircle,
                completed = true,
            )
        }
    }
}

@Composable
internal fun CompletionSummary(title: String, subtitle: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            Modifier.fillMaxWidth().padding(AppSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background) {
                Icon(
                    AppIcons.CheckCircle,
                    null,
                    Modifier.padding(AppSpacing.small).size(AppDimensions.icon),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
