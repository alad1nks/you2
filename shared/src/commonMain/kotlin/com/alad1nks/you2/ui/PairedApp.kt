package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alad1nks.you2.core.model.DateStatus
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.status
import com.alad1nks.you2.feature.designsystem.components.AppTextField
import com.alad1nks.you2.feature.designsystem.components.Avatar
import com.alad1nks.you2.feature.designsystem.components.CategoryChip
import com.alad1nks.you2.feature.designsystem.components.ContentCard
import com.alad1nks.you2.feature.designsystem.components.CoupleHeader
import com.alad1nks.you2.feature.designsystem.components.DateBadge
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.components.StatusTone
import com.alad1nks.you2.feature.designsystem.components.WishCard
import com.alad1nks.you2.feature.designsystem.components.WishCover
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.add_wish
import com.alad1nks.you2.resources.generated.added_by
import com.alad1nks.you2.resources.generated.adventures
import com.alad1nks.you2.resources.generated.all_wishes
import com.alad1nks.you2.resources.generated.back
import com.alad1nks.you2.resources.generated.category_adventures
import com.alad1nks.you2.resources.generated.category_movies
import com.alad1nks.you2.resources.generated.category_other
import com.alad1nks.you2.resources.generated.category_plans
import com.alad1nks.you2.resources.generated.category_presents
import com.alad1nks.you2.resources.generated.category_tv_series
import com.alad1nks.you2.resources.generated.connected
import com.alad1nks.you2.resources.generated.date_range
import com.alad1nks.you2.resources.generated.demo_signout
import com.alad1nks.you2.resources.generated.due_today
import com.alad1nks.you2.resources.generated.empty_list
import com.alad1nks.you2.resources.generated.ended
import com.alad1nks.you2.resources.generated.icon_couple
import com.alad1nks.you2.resources.generated.icon_heart
import com.alad1nks.you2.resources.generated.icon_memories
import com.alad1nks.you2.resources.generated.in_progress
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.movies
import com.alad1nks.you2.resources.generated.no_date
import com.alad1nks.you2.resources.generated.other
import com.alad1nks.you2.resources.generated.our_memories
import com.alad1nks.you2.resources.generated.our_wishes
import com.alad1nks.you2.resources.generated.overdue
import com.alad1nks.you2.resources.generated.plans
import com.alad1nks.you2.resources.generated.presents
import com.alad1nks.you2.resources.generated.scaffold_notice
import com.alad1nks.you2.resources.generated.shared_space
import com.alad1nks.you2.resources.generated.subtitle
import com.alad1nks.you2.resources.generated.summary_counts
import com.alad1nks.you2.resources.generated.summary_title
import com.alad1nks.you2.resources.generated.title_label
import com.alad1nks.you2.resources.generated.tv_series
import com.alad1nks.you2.resources.generated.upcoming_deadline
import com.alad1nks.you2.resources.generated.upcoming_range
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppContent(space: PairedSpace, today: LocalDate) {
    val navigation = rememberPairedNavigation()
    val wishesScroll = rememberLazyListState()
    val memoriesScroll = rememberLazyListState()
    val usScroll = rememberLazyListState()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter) {
            Scaffold(
                modifier = Modifier.widthIn(max = AppDimensions.contentWidth).fillMaxSize().safeDrawingPadding(),
                topBar = { CoupleHeader(space.couple.you.name, space.couple.partner.name) },
                bottomBar = {
                    Column {
                        if (navigation.tab == MainTab.WISHES) {
                            PrimaryButton(
                                stringResource(Res.string.add_wish),
                                { navigation.open(OverlayRoute.Form()) },
                                Modifier.padding(horizontal = AppSpacing.page, vertical = AppSpacing.small),
                            )
                        }
                        BottomNavigation(navigation.tab, navigation::select)
                    }
                },
            ) { padding ->
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize().padding(padding).padding(horizontal = AppSpacing.page)
                            .testTag("paired-list"),
                    state =
                        when (navigation.tab) {
                            MainTab.WISHES -> wishesScroll
                            MainTab.MEMORIES -> memoriesScroll
                            MainTab.US -> usScroll
                        },
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AppSpacing.large),
                ) {
                    item {
                        Text(
                            stringResource(
                                when (navigation.tab) {
                                    MainTab.WISHES -> Res.string.our_wishes
                                    MainTab.MEMORIES -> Res.string.our_memories
                                    MainTab.US -> Res.string.shared_space
                                },
                            ),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            stringResource(Res.string.subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(AppSpacing.large),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(
                                    stringResource(Res.string.summary_title),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    stringResource(Res.string.summary_counts, space.active.size, space.memories.size),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            stringResource(Res.string.scaffold_notice),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (navigation.tab == MainTab.US) {
                        item {
                            ContentCard {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Avatar(space.couple.you.name)
                                    Text(space.couple.you.name)
                                    Avatar(space.couple.partner.name)
                                    Text(space.couple.partner.name)
                                }
                                Text(stringResource(Res.string.connected))
                                Text(
                                    stringResource(Res.string.demo_signout),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    } else {
                        if (navigation.tab == MainTab.WISHES) {
                            item {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                                    CategoryChip(
                                        stringResource(Res.string.all_wishes),
                                        navigation.category == null,
                                        { navigation.category = null },
                                    )
                                    WishCategory.entries.forEach { category ->
                                        CategoryChip(
                                            stringResource(category.label),
                                            navigation.category == category,
                                            { navigation.category = category },
                                        )
                                    }
                                }
                            }
                        }
                        val wishes =
                            if (navigation.tab == MainTab.WISHES) {
                                space.active.filter {
                                    navigation.category == null || it.content.category == navigation.category
                                }
                            } else {
                                space.memories.sortedByDescending { it.completion?.date }
                            }
                        if (wishes.isEmpty()) item { Text(stringResource(Res.string.empty_list)) }
                        items(wishes, key = { it.id }) { wish ->
                            WishCard(
                                wish.content.title,
                                wish.content.description,
                                stringResource(
                                    Res.string.added_by,
                                    if (wish.authorId == space.couple.you.id) {
                                        space.couple.you.name
                                    } else {
                                        space.couple.partner.name
                                    },
                                ),
                                onClick = { navigation.open(OverlayRoute.Details(wish.id)) },
                                date = { WishDateBadge(wish.content.dates, today) },
                            )
                        }
                    }
                }
            }
            navigation.overlay?.let { route ->
                Dialog(
                    onDismissRequest = navigation::back,
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    Surface(
                        Modifier.widthIn(
                            max = AppDimensions.contentWidth,
                        ).fillMaxSize().safeDrawingPadding().imePadding(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column {
                            TextButton(navigation::back) { Text(stringResource(Res.string.back)) }
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(AppSpacing.page),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
                            ) {
                                when (route) {
                                    is OverlayRoute.Details -> {
                                        val wish = space.wishes.find { it.id == route.wishId }
                                        if (wish != null) {
                                            item {
                                                Text(
                                                    wish.content.title,
                                                    style = MaterialTheme.typography.headlineLarge,
                                                )
                                            }
                                            item {
                                                WishCover(
                                                    (wish.content.cover?.category ?: wish.content.category).image,
                                                )
                                            }
                                            item { WishDateBadge(wish.content.dates, today) }
                                            item { ContentCard { Text(wish.content.description) } }
                                        }
                                    }

                                    is OverlayRoute.Form -> {
                                        item {
                                            Text(
                                                stringResource(Res.string.add_wish),
                                                style = MaterialTheme.typography.headlineLarge,
                                            )
                                        }
                                        item { FormPreview() }
                                    }
                                }
                                item {
                                    Text(
                                        stringResource(Res.string.scaffold_notice),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormPreview() {
    var title by rememberSaveable { mutableStateOf("") }
    AppTextField(title, { title = it }, stringResource(Res.string.title_label))
}

@Composable
private fun BottomNavigation(selected: MainTab, onSelect: (MainTab) -> Unit) {
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
                onClick = {
                    onSelect(tab)
                },
                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                icon = {
                    Icon(
                        painterResource(
                            when (tab) {
                                MainTab.WISHES -> Res.drawable.icon_heart
                                MainTab.MEMORIES -> Res.drawable.icon_memories
                                MainTab.US -> Res.drawable.icon_couple
                            },
                        ),
                        null,
                        Modifier.size(AppDimensions.icon),
                    )
                },
            )
        }
    }
}

internal val WishCategory.label: StringResource
    get() =
        when (this) {
            WishCategory.ADVENTURES -> Res.string.adventures
            WishCategory.PRESENTS -> Res.string.presents
            WishCategory.PLANS -> Res.string.plans
            WishCategory.MOVIES -> Res.string.movies
            WishCategory.TV_SERIES -> Res.string.tv_series
            WishCategory.OTHER -> Res.string.other
        }

internal val WishCategory.image: DrawableResource
    get() =
        when (this) {
            WishCategory.ADVENTURES -> Res.drawable.category_adventures
            WishCategory.PRESENTS -> Res.drawable.category_presents
            WishCategory.PLANS -> Res.drawable.category_plans
            WishCategory.MOVIES -> Res.drawable.category_movies
            WishCategory.TV_SERIES -> Res.drawable.category_tv_series
            WishCategory.OTHER -> Res.drawable.category_other
        }

@Composable
private fun WishDateBadge(dates: WishDates, today: LocalDate) {
    val status = dates.status(today)
    if (status == DateStatus.NONE) return
    val date =
        when (dates) {
            WishDates.NoDate -> ""
            is WishDates.Deadline -> dates.date.toString()
            is WishDates.Range -> stringResource(Res.string.date_range, dates.start.toString(), dates.end.toString())
        }
    val label =
        when (status) {
            DateStatus.NONE -> Res.string.no_date
            DateStatus.UPCOMING_DEADLINE -> Res.string.upcoming_deadline
            DateStatus.TODAY -> Res.string.due_today
            DateStatus.OVERDUE -> Res.string.overdue
            DateStatus.UPCOMING_RANGE -> Res.string.upcoming_range
            DateStatus.IN_PROGRESS -> Res.string.in_progress
            DateStatus.ENDED -> Res.string.ended
        }
    DateBadge(
        stringResource(label, date),
        when (status) {
            DateStatus.OVERDUE, DateStatus.ENDED -> StatusTone.ERROR
            DateStatus.TODAY -> StatusTone.TODAY
            DateStatus.IN_PROGRESS -> StatusTone.ACTIVE
            else -> StatusTone.NEUTRAL
        },
    )
}
