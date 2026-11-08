package com.alad1nks.you2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.Couple
import com.alad1nks.you2.core.model.Wish
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.Avatar
import com.alad1nks.you2.feature.designsystem.components.BackTextButton
import com.alad1nks.you2.feature.designsystem.components.ContentCard
import com.alad1nks.you2.feature.designsystem.components.DeleteWishDialog
import com.alad1nks.you2.feature.designsystem.components.MenuAction
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.components.WishMenu
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.added_by
import com.alad1nks.you2.resources.generated.added_by_you
import com.alad1nks.you2.resources.generated.back
import com.alad1nks.you2.resources.generated.back_to_memories
import com.alad1nks.you2.resources.generated.completed
import com.alad1nks.you2.resources.generated.completed_by
import com.alad1nks.you2.resources.generated.completed_by_you
import com.alad1nks.you2.resources.generated.completed_on
import com.alad1nks.you2.resources.generated.completion_hint
import com.alad1nks.you2.resources.generated.delete_wish
import com.alad1nks.you2.resources.generated.details
import com.alad1nks.you2.resources.generated.edit_memory
import com.alad1nks.you2.resources.generated.edit_wish
import com.alad1nks.you2.resources.generated.for_couple
import com.alad1nks.you2.resources.generated.little_memory
import com.alad1nks.you2.resources.generated.mark_done
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.no_memory
import com.alad1nks.you2.resources.generated.original_wish
import com.alad1nks.you2.resources.generated.restore
import com.alad1nks.you2.resources.generated.shared_adventures
import com.alad1nks.you2.resources.generated.shared_movies
import com.alad1nks.you2.resources.generated.shared_other
import com.alad1nks.you2.resources.generated.shared_plans
import com.alad1nks.you2.resources.generated.shared_presents
import com.alad1nks.you2.resources.generated.shared_tv_series
import com.alad1nks.you2.resources.generated.shared_wish
import com.alad1nks.you2.resources.generated.view_image
import com.alad1nks.you2.resources.generated.waiting_to_happen
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WishDetails(
    wish: Wish,
    couple: Couple,
    today: LocalDate,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit = {},
    onEditMemory: (String) -> Unit = {},
) {
    val completion = wish.completion
    var editMemoryOpen by rememberSaveable(wish.id) { mutableStateOf(false) }
    var menuOpen by rememberSaveable(wish.id) { mutableStateOf(false) }
    var deleteOpen by rememberSaveable(wish.id) { mutableStateOf(false) }
    val images = listOf(wish.content.cover ?: WishImage(wish.content.category)) + wish.content.attachments
    val imageStates = images.map { image -> key(wish.id, image) { wishImageState(image) } }
    var viewerIndex by rememberSaveable(wish.id, images) { mutableStateOf<Int?>(null) }
    BoxWithConstraints {
        val layout = AppLayout(maxWidth)
        val desktop = LocalDesktopLayout.current
        Scaffold(
            topBar = {
                Row(
                    Modifier.fillMaxWidth().heightIn(
                        min = AppDimensions.headerHeight,
                    ).padding(horizontal = if (desktop) 40.dp else AppSpacing.page),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackTextButton(
                        stringResource(if (completion == null) Res.string.back else Res.string.memories),
                        onBack,
                    )
                    WishMenu(
                        menuOpen,
                        { menuOpen = it },
                        buildList {
                            if (wish.completion == null) {
                                add(MenuAction(stringResource(Res.string.edit_wish), onEdit, AppIcons.Edit))
                            } else {
                                add(
                                    MenuAction(
                                        stringResource(Res.string.edit_memory),
                                        { editMemoryOpen = true },
                                        AppIcons.Edit,
                                    ),
                                )
                                add(
                                    MenuAction(
                                        stringResource(Res.string.restore),
                                        onRestore,
                                        AppIcons.Undo,
                                    ),
                                )
                            }
                            add(
                                MenuAction(
                                    stringResource(Res.string.delete_wish),
                                    { deleteOpen = true },
                                    AppIcons.DeleteOutline,
                                    true,
                                ),
                            )
                        },
                    )
                }
            },
            bottomBar = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    PrimaryButton(
                        stringResource(if (completion == null) Res.string.mark_done else Res.string.back_to_memories),
                        if (completion == null) onComplete else onBack,
                        Modifier.workspaceAction(desktop, layout.actionInset),
                    )
                }
            },
        ) { padding ->
            AdaptiveContent(
                Modifier.fillMaxSize().padding(padding).testTag("wish-details"),
                splitAt = 2,
                heading = false,
                gap = AppSpacing.detailsGap,
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(shape = AppShapes.pill, color = MaterialTheme.colorScheme.surfaceContainer) {
                                Text(
                                    stringResource(wish.content.category.label),
                                    Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.xs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                stringResource(
                                    if (wish.completion == null) Res.string.waiting_to_happen else Res.string.completed,
                                ),
                                Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Text(wish.content.title, style = MaterialTheme.typography.headlineLarge)
                    }
                }
                item {
                    WishCover(
                        imageStates.first(),
                        Modifier.testTag("wish-cover").clip(AppShapes.image).clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(Res.string.view_image, 1),
                        ) { viewerIndex = 0 },
                        height =
                            if (layout.tablet) {
                                260.dp
                            } else if (completion == null) {
                                160.dp
                            } else {
                                144.dp
                            },
                    )
                }
                if (completion != null) {
                    item {
                        CompletionSummary(
                            stringResource(Res.string.completed_on, wishDateText(completion.date)),
                            if (completion.authorId == couple.you.id) {
                                stringResource(Res.string.completed_by_you)
                            } else {
                                stringResource(Res.string.completed_by, couple.partner.name)
                            },
                        )
                    }
                }
                item {
                    val author = if (wish.authorId == couple.you.id) couple.you else couple.partner
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(author.name)
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            Text(
                                if (author.id == couple.you.id) {
                                    stringResource(
                                        Res.string.added_by_you,
                                    )
                                } else {
                                    stringResource(Res.string.added_by, author.name)
                                },
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                stringResource(Res.string.for_couple, couple.partner.name),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                if (wish.content.dates != WishDates.NoDate) {
                    item { WishDateBadge(wish.content.dates, today) }
                }
                if (wish.content.description.isNotBlank()) {
                    item {
                        ContentCard {
                            Text(
                                stringResource(
                                    if (completion == null) Res.string.details else Res.string.original_wish,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(wish.content.description)
                        }
                    }
                }
                itemsIndexed(wish.content.attachments) { index, _ ->
                    WishCover(
                        imageStates[index + 1],
                        Modifier.then(
                            if (layout.tablet) Modifier.width(144.dp) else Modifier,
                        ).testTag("wish-attachment-$index").clip(AppShapes.image).clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(Res.string.view_image, index + 2),
                        ) { viewerIndex = index + 1 },
                        height = if (layout.tablet) 96.dp else 160.dp,
                        showLoadingLabel = false,
                    )
                }
                if (completion != null) {
                    item {
                        ContentCard {
                            Text(stringResource(Res.string.little_memory), style = MaterialTheme.typography.labelLarge)
                            Text(
                                completion.memory.ifBlank { stringResource(Res.string.no_memory) },
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                } else {
                    item {
                        Surface(shape = AppShapes.card, color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(
                                Modifier.fillMaxWidth().padding(AppSpacing.large),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(shape = AppShapes.pill, color = MaterialTheme.colorScheme.background) {
                                    Icon(
                                        AppIcons.FavoriteBorder,
                                        null,
                                        Modifier.padding(AppSpacing.small).size(AppDimensions.icon),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    Text(
                                        stringResource(Res.string.shared_wish),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Text(
                                        stringResource(wish.content.category.sharedHint),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
                if (wish.completion == null) {
                    item {
                        Text(
                            stringResource(Res.string.completion_hint),
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
        viewerIndex?.let { index ->
            key(wish.id, images) {
                ImageViewer(imageStates, index, wish.content.title) { viewerIndex = null }
            }
        }
        if (editMemoryOpen && completion != null) {
            MemoryEditor(
                wish.id,
                completion.memory,
                onDismiss = { editMemoryOpen = false },
                onSave = {
                    onEditMemory(it)
                    editMemoryOpen = false
                },
            )
        }
    }
    if (deleteOpen) {
        DeleteWishDialog(wish.content.title, couple.partner.name, { deleteOpen = false }, {
            deleteOpen = false
            onDelete()
        })
    }
}

private val WishCategory.sharedHint
    get() =
        when (this) {
            WishCategory.ADVENTURES -> Res.string.shared_adventures
            WishCategory.PRESENTS -> Res.string.shared_presents
            WishCategory.PLANS -> Res.string.shared_plans
            WishCategory.MOVIES -> Res.string.shared_movies
            WishCategory.TV_SERIES -> Res.string.shared_tv_series
            WishCategory.OTHER -> Res.string.shared_other
        }
