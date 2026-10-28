package com.alad1nks.you2.feature.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.brand
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.cannot_undo
import com.alad1nks.you2.resources.generated.character_count
import com.alad1nks.you2.resources.generated.couple_label
import com.alad1nks.you2.resources.generated.delete_action
import com.alad1nks.you2.resources.generated.delete_message
import com.alad1nks.you2.resources.generated.delete_title
import com.alad1nks.you2.resources.generated.field_error
import com.alad1nks.you2.resources.generated.icon_calendar
import com.alad1nks.you2.resources.generated.icon_heart
import com.alad1nks.you2.resources.generated.icon_more
import com.alad1nks.you2.resources.generated.more_options
import com.alad1nks.you2.resources.generated.pair_initials
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick,
        modifier.fillMaxWidth().heightIn(min = AppDimensions.buttonHeight),
        enabled = enabled,
        shape = AppShapes.pill,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            Text(text)
        }
    }
}

@Composable
fun CoupleHeader(yourName: String, partnerName: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().heightIn(min = AppDimensions.headerHeight).padding(horizontal = AppSpacing.page),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(Res.drawable.icon_heart),
                null,
                Modifier.size(AppDimensions.icon),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(Res.string.brand),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Surface(shape = AppShapes.pill, color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(Modifier.padding(AppSpacing.medium), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(
                    stringResource(Res.string.pair_initials, yourName.take(1), partnerName.take(1)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(stringResource(Res.string.couple_label, partnerName), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    Surface(
        modifier.size(AppDimensions.avatar),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Box(contentAlignment = Alignment.Center) { Text(name.take(1), color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) ({ Text("✓") }) else null,
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            ),
        modifier = modifier,
    )
}

@Composable
fun WishCard(
    title: String,
    description: String,
    author: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    date: @Composable () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 94.dp),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(AppSpacing.cardInset),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = AppShapes.symbol, color = MaterialTheme.colorScheme.surfaceContainer) {
                Icon(
                    painterResource(Res.drawable.icon_heart),
                    null,
                    Modifier.padding(horizontal = 10.dp, vertical = 14.dp).size(AppDimensions.icon),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (description.isNotBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                date()
            }
        }
    }
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth(),
        shape = AppShapes.card,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(
            Modifier.padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
            content = content,
        )
    }
}

@Composable
fun WishCover(image: DrawableResource, modifier: Modifier = Modifier) {
    Image(
        painterResource(image),
        null,
        modifier.fillMaxWidth().heightIn(min = 160.dp, max = 160.dp).clip(AppShapes.image),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    maxLength: Int = 80,
    singleLine: Boolean = true,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value,
        onValueChange,
        modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        shape = AppShapes.field,
        isError = isError || value.length > maxLength,
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isError) stringResource(Res.string.field_error) else "")
                Text(stringResource(Res.string.character_count, value.length, maxLength))
            }
        },
    )
}

enum class StatusTone { NEUTRAL, TODAY, ACTIVE, ERROR }

@Composable
fun DateBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
    val background =
        when (tone) {
            StatusTone.NEUTRAL -> colors.surfaceContainer
            StatusTone.TODAY -> if (isDark) Color(0xFF594000) else Color(0xFFFFF0CD)
            StatusTone.ACTIVE -> colors.primaryContainer
            StatusTone.ERROR -> colors.errorContainer
        }
    val foreground =
        when (tone) {
            StatusTone.NEUTRAL -> colors.onSurfaceVariant
            StatusTone.TODAY -> if (isDark) Color(0xFFFFDF99) else Color(0xFF754B00)
            StatusTone.ACTIVE -> colors.onPrimaryContainer
            StatusTone.ERROR -> colors.onErrorContainer
        }
    Surface(modifier.fillMaxWidth(), shape = AppShapes.date, color = background) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(Res.drawable.icon_calendar),
                null,
                Modifier.size(AppDimensions.dateIcon),
                tint = foreground,
            )
            Text(text, color = foreground, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class MenuAction(val label: String, val onClick: () -> Unit)

@Composable
fun WishMenu(expanded: Boolean, onExpandedChange: (Boolean) -> Unit, actions: List<MenuAction>) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                painterResource(Res.drawable.icon_more),
                stringResource(Res.string.more_options),
                Modifier.size(AppDimensions.icon),
            )
        }
        DropdownMenu(expanded, onDismissRequest = { onExpandedChange(false) }) {
            actions.forEach {
                action ->
                DropdownMenuItem(text = { Text(action.label) }, onClick = {
                    onExpandedChange(false)
                    action.onClick()
                })
            }
        }
    }
}

@Composable
fun DeleteWishDialog(title: String, partnerName: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                Text(stringResource(Res.string.delete_message, title, partnerName))
                Text(stringResource(Res.string.cannot_undo), style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
        confirmButton = {
            Button(onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(Res.string.delete_action))
            }
        },
    )
}
