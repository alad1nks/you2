package com.alad1nks.you2.feature.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alad1nks.you2.feature.designsystem.AppIcons
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
            Text(text, textAlign = TextAlign.Center)
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
                AppIcons.FavoriteBorder,
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
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Surface(
        modifier.size(AppDimensions.avatar),
        shape = CircleShape,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.take(1), color = MaterialTheme.colorScheme.primary, style = textStyle)
        }
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
    symbol: ImageVector = AppIcons.FavoriteBorder,
    completed: Boolean = false,
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
            Surface(
                shape = AppShapes.symbol,
                color =
                    if (completed) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ) {
                Icon(
                    symbol,
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
fun WishCover(image: DrawableResource, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 160.dp) {
    Image(
        painterResource(image),
        null,
        modifier.fillMaxWidth().heightIn(min = height, max = height).clip(AppShapes.image),
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
    minLines: Int = 1,
    labelAbove: Boolean = false,
    helperText: String? = null,
    placeholder: String? = null,
    labelTrailingText: String? = null,
    showLengthError: Boolean = true,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        if (labelAbove) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                labelTrailingText?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        val supportingContent: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(
                    if (isError) stringResource(Res.string.field_error) else helperText.orEmpty(),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                )
                Text(
                    stringResource(Res.string.character_count, value.length, maxLength),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        OutlinedTextField(
            value,
            onValueChange,
            modifier.fillMaxWidth().semantics { if (labelAbove) contentDescription = label },
            label = if (labelAbove) null else ({ Text(label) }),
            placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.secondary) } },
            singleLine = singleLine,
            minLines = minLines,
            shape = AppShapes.field,
            colors =
                if (labelAbove) {
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                },
            isError = isError || (showLengthError && value.length > maxLength),
            supportingText =
                if (labelAbove) {
                    null
                } else {
                    {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                            Text(
                                if (isError) stringResource(Res.string.field_error) else helperText.orEmpty(),
                                Modifier.weight(1f),
                            )
                            Text(stringResource(Res.string.character_count, value.length, maxLength))
                        }
                    }
                },
        )
        if (labelAbove) supportingContent()
    }
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
                AppIcons.CalendarToday,
                null,
                Modifier.size(AppDimensions.dateIcon),
                tint = foreground,
            )
            Text(text, color = foreground, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class MenuAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
)

@Composable
fun WishMenu(expanded: Boolean, onExpandedChange: (Boolean) -> Unit, actions: List<MenuAction>) {
    val menuFocus = remember { FocusRequester() }
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                AppIcons.MoreHoriz,
                stringResource(Res.string.more_options),
                Modifier.size(AppDimensions.icon),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        DropdownMenu(
            expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier =
                Modifier.width(220.dp).onPreviewKeyEvent {
                    if (it.key == Key.Escape || it.key == Key.Back) {
                        onExpandedChange(false)
                        true
                    } else {
                        false
                    }
                }.focusRequester(menuFocus).focusable(),
            shape = AppShapes.field,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            LaunchedEffect(expanded) {
                if (expanded) menuFocus.requestFocus()
            }
            actions.forEach {
                action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            action.label,
                            color =
                                if (action.destructive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    },
                    leadingIcon =
                        action.icon?.let {
                            icon ->
                            {
                                Icon(
                                    icon,
                                    null,
                                    Modifier.size(20.dp),
                                    tint =
                                        if (action.destructive) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                )
                            }
                        },
                    onClick = {
                        onExpandedChange(false)
                        action.onClick()
                    },
                )
            }
        }
    }
}

@Composable
fun DeleteWishDialog(title: String, partnerName: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            Modifier.padding(horizontal = AppSpacing.page).widthIn(max = 364.dp).fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(AppSpacing.page),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
            ) {
                Text(stringResource(Res.string.delete_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(Res.string.delete_message, title, partnerName),
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    stringResource(Res.string.cannot_undo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                ) {
                    OutlinedButton(onDismiss, Modifier.weight(1f).widthIn(min = 136.dp).heightIn(min = 48.dp)) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(
                        onDelete,
                        Modifier.weight(1f).widthIn(min = 136.dp).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text(stringResource(Res.string.delete_action))
                    }
                }
            }
        }
    }
}
