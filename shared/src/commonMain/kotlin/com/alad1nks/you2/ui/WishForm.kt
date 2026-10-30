package com.alad1nks.you2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.Couple
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.AppTextField
import com.alad1nks.you2.feature.designsystem.components.BackTextButton
import com.alad1nks.you2.feature.designsystem.components.CategoryChip
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.add_images
import com.alad1nks.you2.resources.generated.add_main_image
import com.alad1nks.you2.resources.generated.add_to_wishes
import com.alad1nks.you2.resources.generated.add_wish
import com.alad1nks.you2.resources.generated.attachments_hint
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.choose_category
import com.alad1nks.you2.resources.generated.choose_date
import com.alad1nks.you2.resources.generated.choose_images
import com.alad1nks.you2.resources.generated.complete_by
import com.alad1nks.you2.resources.generated.couple_label
import com.alad1nks.you2.resources.generated.cover_hint
import com.alad1nks.you2.resources.generated.cover_selected_hint
import com.alad1nks.you2.resources.generated.dates_error
import com.alad1nks.you2.resources.generated.deadline_hint
import com.alad1nks.you2.resources.generated.deadline_mode
import com.alad1nks.you2.resources.generated.description_hint
import com.alad1nks.you2.resources.generated.description_images
import com.alad1nks.you2.resources.generated.description_placeholder
import com.alad1nks.you2.resources.generated.edit_wish
import com.alad1nks.you2.resources.generated.end_date
import com.alad1nks.you2.resources.generated.field_optional
import com.alad1nks.you2.resources.generated.field_required
import com.alad1nks.you2.resources.generated.form_description
import com.alad1nks.you2.resources.generated.form_shared_hint
import com.alad1nks.you2.resources.generated.form_shared_title
import com.alad1nks.you2.resources.generated.form_subtitle
import com.alad1nks.you2.resources.generated.form_title
import com.alad1nks.you2.resources.generated.local_images_hint
import com.alad1nks.you2.resources.generated.main_image
import com.alad1nks.you2.resources.generated.no_date
import com.alad1nks.you2.resources.generated.no_date_hint
import com.alad1nks.you2.resources.generated.pair_initials
import com.alad1nks.you2.resources.generated.range_hint
import com.alad1nks.you2.resources.generated.range_mode
import com.alad1nks.you2.resources.generated.remove_attachment
import com.alad1nks.you2.resources.generated.remove_image
import com.alad1nks.you2.resources.generated.replace_image
import com.alad1nks.you2.resources.generated.save_changes
import com.alad1nks.you2.resources.generated.select_date
import com.alad1nks.you2.resources.generated.start_date
import com.alad1nks.you2.resources.generated.title_hint
import com.alad1nks.you2.resources.generated.title_placeholder
import com.alad1nks.you2.resources.generated.use_images
import com.alad1nks.you2.resources.generated.when_label
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WishForm(
    wishId: String?,
    initial: WishDraft?,
    couple: Couple,
    today: LocalDate,
    onCancel: () -> Unit,
    onSave: (WishDraft) -> Unit,
) {
    val state = rememberSaveable(wishId, saver = WishFormState.Saver) { WishFormState(initial) }
    var pickingImage by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingDate by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollState = rememberLazyListState()
    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().heightIn(
                    min = AppDimensions.headerHeight,
                ).padding(horizontal = AppSpacing.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackTextButton(stringResource(Res.string.cancel), onCancel)
                Surface(shape = AppShapes.pill, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        Modifier.padding(AppSpacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    ) {
                        Text(
                            stringResource(
                                Res.string.pair_initials,
                                couple.you.name.take(1),
                                couple.partner.name.take(1),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            stringResource(Res.string.couple_label, couple.partner.name),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box {
                PrimaryButton(
                    stringResource(if (wishId == null) Res.string.add_to_wishes else Res.string.save_changes),
                    { state.draftOrNull()?.let(onSave) },
                    Modifier.padding(horizontal = AppSpacing.page, vertical = AppSpacing.listGap).testTag("save-wish"),
                    enabled = state.draftOrNull() != null,
                )
                if (scrollState.canScrollForward) {
                    val background = MaterialTheme.colorScheme.background
                    Box(
                        Modifier.fillMaxWidth().height(AppSpacing.section).offset(y = -AppSpacing.section)
                            .background(Brush.verticalGradient(listOf(background.copy(alpha = 0f), background))),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).testTag("wish-form"),
            state = scrollState,
            contentPadding = PaddingValues(horizontal = AppSpacing.page, vertical = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.page),
        ) {
            item {
                Text(
                    stringResource(if (wishId == null) Res.string.add_wish else Res.string.edit_wish),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    stringResource(Res.string.form_subtitle),
                    Modifier.padding(top = AppSpacing.xs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            item {
                AppTextField(
                    state.title,
                    { state.title = it },
                    stringResource(Res.string.form_title),
                    Modifier.testTag("wish-title"),
                    showLengthError = false,
                    placeholder = stringResource(Res.string.title_placeholder),
                    labelTrailingText = stringResource(Res.string.field_required),
                    labelAbove = true,
                    helperText = stringResource(Res.string.title_hint),
                )
            }
            item {
                FormLabel(stringResource(Res.string.choose_category))
                FlowRow(
                    Modifier.padding(top = AppSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    maxItemsInEachRow = 3,
                ) {
                    WishCategory.entries.forEach { category ->
                        CategoryChip(
                            stringResource(category.label),
                            state.category == category,
                            { state.category = category },
                            Modifier.height(32.dp),
                        )
                    }
                }
            }
            item {
                FormLabel(stringResource(Res.string.when_label), optional = true)
                FlowRow(
                    Modifier.padding(top = AppSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                ) {
                    WishDateMode.entries.forEach { mode ->
                        CategoryChip(
                            stringResource(
                                when (mode) {
                                    WishDateMode.NONE -> Res.string.no_date
                                    WishDateMode.DEADLINE -> Res.string.deadline_mode
                                    WishDateMode.RANGE -> Res.string.range_mode
                                },
                            ),
                            state.mode == mode,
                            { state.selectMode(mode) },
                            Modifier.height(32.dp),
                        )
                    }
                }
                if (state.mode != WishDateMode.NONE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                        DateField(
                            stringResource(
                                if (state.mode == WishDateMode.DEADLINE) {
                                    Res.string.complete_by
                                } else {
                                    Res.string.start_date
                                },
                            ),
                            state.start,
                            Modifier.weight(1f).testTag("start-date"),
                        ) { pickingDate = "start" }
                        if (state.mode == WishDateMode.RANGE) {
                            DateField(
                                stringResource(Res.string.end_date),
                                state.end,
                                Modifier.weight(1f).testTag("end-date"),
                            ) { pickingDate = "end" }
                        }
                    }
                }
                Box(Modifier.padding(top = AppSpacing.small)) {
                    FormHint(
                        stringResource(
                            when {
                                state.datesInvalid -> Res.string.dates_error
                                state.mode == WishDateMode.NONE -> Res.string.no_date_hint
                                state.mode == WishDateMode.DEADLINE -> Res.string.deadline_hint
                                else -> Res.string.range_hint
                            },
                        ),
                        error = state.datesInvalid,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    FormLabel(stringResource(Res.string.main_image), optional = true)
                    val cover = state.cover
                    if (cover == null) {
                        ImageAction(
                            stringResource(Res.string.add_main_image),
                            AppIcons.AddPhotoAlternate,
                        ) { pickingImage = "cover" }
                    } else {
                        Image(
                            painterResource(cover.category.image),
                            stringResource(Res.string.main_image),
                            Modifier.fillMaxWidth().heightIn(min = 116.dp, max = 116.dp).clip(AppShapes.field),
                            contentScale = ContentScale.Crop,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                            OutlinedButton(
                                { pickingImage = "cover" },
                                Modifier.weight(1f),
                            ) { Text(stringResource(Res.string.replace_image)) }
                            OutlinedButton(
                                { state.cover = null },
                                Modifier.weight(1f),
                            ) { Text(stringResource(Res.string.remove_image)) }
                        }
                    }
                    FormHint(
                        stringResource(if (cover == null) Res.string.cover_hint else Res.string.cover_selected_hint),
                    )
                }
            }
            item {
                AppTextField(
                    state.description,
                    { state.description = it },
                    stringResource(Res.string.form_description),
                    Modifier.heightIn(min = 128.dp).testTag("wish-description"),
                    maxLength = 500,
                    singleLine = false,
                    minLines = 4,
                    placeholder = stringResource(Res.string.description_placeholder),
                    labelTrailingText = stringResource(Res.string.field_optional),
                    labelAbove = true,
                    helperText = stringResource(Res.string.description_hint),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    FormLabel(stringResource(Res.string.description_images), optional = true)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    ) {
                        state.attachments.forEachIndexed { index, image ->
                            Box(Modifier.size(104.dp).testTag("form-attachment-$index")) {
                                Image(
                                    painterResource(image.category.image),
                                    stringResource(image.category.label),
                                    Modifier.fillMaxSize().clip(AppShapes.field),
                                    contentScale = ContentScale.Crop,
                                )
                                IconButton(
                                    {
                                        state.attachments =
                                            state.attachments.filterIndexed {
                                                i,
                                                _,
                                                ->
                                                i != index
                                            }
                                    },
                                    Modifier.align(Alignment.TopEnd),
                                ) {
                                    Surface(shape = AppShapes.pill, color = MaterialTheme.colorScheme.background) {
                                        Icon(
                                            AppIcons.DeleteOutline,
                                            stringResource(Res.string.remove_attachment, index + 1),
                                            Modifier.padding(6.dp).size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ImageAction(
                        stringResource(Res.string.add_images),
                        AppIcons.Add,
                        attachments = true,
                    ) { pickingImage = "attachments" }
                    FormHint(stringResource(Res.string.attachments_hint))
                }
            }
            item {
                Surface(shape = AppShapes.card, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(
                        Modifier.fillMaxWidth().padding(AppSpacing.large),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            AppIcons.FavoriteBorder,
                            null,
                            Modifier.clip(AppShapes.pill).background(MaterialTheme.colorScheme.background)
                                .padding(AppSpacing.small).size(AppDimensions.icon),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            FormLabel(stringResource(Res.string.form_shared_title))
                            FormHint(stringResource(Res.string.form_shared_hint, couple.partner.name))
                        }
                    }
                }
            }
        }
    }
    pickingImage?.let { target ->
        DemoImagePicker(multiple = target == "attachments", onDismiss = { pickingImage = null }) { images ->
            if (target == "cover") state.cover = images.first() else state.attachments = state.attachments + images
            pickingImage = null
        }
    }
    pickingDate?.let { target ->
        WishDatePicker(
            if (target == "start") state.start else state.end,
            today,
            onDismiss = { pickingDate = null },
            onSelect = {
                if (target == "start") state.start = it else state.end = it
                pickingDate = null
            },
        )
    }
}

@Composable
private fun FormLabel(text: String, optional: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        if (optional) FormHint(stringResource(Res.string.field_optional))
    }
}

@Composable
private fun FormHint(text: String, error: Boolean = false) =
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
    )

@Composable
private fun ImageAction(label: String, icon: ImageVector, attachments: Boolean = false, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    Surface(
        onClick = onClick,
        shape = AppShapes.field,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier =
            Modifier.fillMaxWidth().drawWithContent {
                drawContent()
                val strokeWidth = 1.dp.toPx()
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style =
                        Stroke(
                            strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                        ),
                )
            },
    ) {
        if (attachments) {
            Column(
                Modifier.heightIn(min = 96.dp).padding(AppSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            ) {
                Icon(icon, null, Modifier.size(AppDimensions.icon), tint = MaterialTheme.colorScheme.primary)
                Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Row(
                Modifier.heightIn(min = 72.dp).padding(AppSpacing.large),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    null,
                    Modifier.clip(AppShapes.symbol).background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(AppSpacing.small).size(AppDimensions.icon),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    FormHint(stringResource(Res.string.local_images_hint))
                }
            }
        }
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        FormHint(label)
        OutlinedButton(
            onClick,
            Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = AppShapes.field,
            contentPadding = PaddingValues(AppSpacing.small),
        ) {
            Text(if (date == null) stringResource(Res.string.choose_date) else wishDateText(date), Modifier.weight(1f))
            Icon(AppIcons.CalendarToday, null, Modifier.size(AppDimensions.dateIcon))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishDatePicker(
    initial: LocalDate?,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    // Material date pickers use midnight UTC, independent of the device time zone.
    val picker =
        rememberDatePickerState(
            initialSelectedDateMillis = initial?.toEpochDays()?.times(86_400_000L),
            initialDisplayedMonthMillis = (initial ?: today).toEpochDays() * 86_400_000L,
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                { picker.selectedDateMillis?.let { onSelect(LocalDate.fromEpochDays((it / 86_400_000L).toInt())) } },
                enabled = picker.selectedDateMillis != null,
            ) { Text(stringResource(Res.string.select_date)) }
        },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
    ) { DatePicker(picker) }
}

@Composable
private fun DemoImagePicker(multiple: Boolean, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(emptyList<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.choose_images)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                item { FormHint(stringResource(Res.string.local_images_hint)) }
                WishCategory.entries.forEach { category ->
                    item {
                        Surface(
                            modifier =
                                Modifier.testTag("demo-image-${category.name}").semantics {
                                    this.selected = category.name in selected
                                },
                            onClick = {
                                selected =
                                    if (!multiple) {
                                        listOf(category.name)
                                    } else if (category.name in selected) {
                                        selected - category.name
                                    } else {
                                        selected + category.name
                                    }
                            },
                            shape = AppShapes.field,
                            border =
                                BorderStroke(
                                    if (category.name in selected) 3.dp else 1.dp,
                                    if (category.name in selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(AppSpacing.small),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                            ) {
                                Image(
                                    painterResource(category.image),
                                    null,
                                    Modifier.width(64.dp).heightIn(min = 48.dp, max = 48.dp).clip(AppShapes.symbol),
                                    contentScale = ContentScale.Crop,
                                )
                                Text(stringResource(category.label))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                { onSelect(selected.map { WishImage(WishCategory.valueOf(it)) }) },
                enabled = selected.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.use_images))
            }
        },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
