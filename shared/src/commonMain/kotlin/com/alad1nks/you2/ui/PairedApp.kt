package com.alad1nks.you2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.DateStatus
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.status
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.CoupleHeader
import com.alad1nks.you2.feature.designsystem.components.DateBadge
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.components.StatusTone
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.add_wish
import com.alad1nks.you2.resources.generated.adventures
import com.alad1nks.you2.resources.generated.back
import com.alad1nks.you2.resources.generated.category_adventures
import com.alad1nks.you2.resources.generated.category_movies
import com.alad1nks.you2.resources.generated.category_other
import com.alad1nks.you2.resources.generated.category_plans
import com.alad1nks.you2.resources.generated.category_presents
import com.alad1nks.you2.resources.generated.category_tv_series
import com.alad1nks.you2.resources.generated.date_range
import com.alad1nks.you2.resources.generated.due_today
import com.alad1nks.you2.resources.generated.ended
import com.alad1nks.you2.resources.generated.formatted_date
import com.alad1nks.you2.resources.generated.in_progress
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.movies
import com.alad1nks.you2.resources.generated.no_date
import com.alad1nks.you2.resources.generated.other
import com.alad1nks.you2.resources.generated.overdue
import com.alad1nks.you2.resources.generated.plans
import com.alad1nks.you2.resources.generated.presents
import com.alad1nks.you2.resources.generated.same_month_range
import com.alad1nks.you2.resources.generated.short_months
import com.alad1nks.you2.resources.generated.tv_series
import com.alad1nks.you2.resources.generated.upcoming_deadline
import com.alad1nks.you2.resources.generated.upcoming_range
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppContent(
    space: PairedSpace,
    today: LocalDate,
    onDeleteWish: (String) -> Unit = {},
    onCompleteWish: (String) -> Unit = {},
    onSaveWish: (String?, WishDraft) -> Unit = { _, _ -> },
    onRestoreWish: (String) -> Unit = {},
    onEditMemory: (String, String) -> Unit = { _, _ -> },
    darkTheme: Boolean = false,
    language: Language = Language.SYSTEM,
    onRenameUser: (String) -> Unit = {},
    onSetDarkTheme: (Boolean) -> Unit = {},
    onSetLanguage: (Language) -> Unit = {},
    onSignOut: (() -> Unit)? = null,
    initialTab: MainTab = MainTab.WISHES,
    onTabChanged: (MainTab) -> Unit = {},
) {
    var usDialog by rememberSaveable { mutableStateOf<UsDialog?>(null) }
    val navigation = rememberPairedNavigation(initialTab)
    SideEffect { onTabChanged(navigation.tab) }
    val wishesScroll = rememberLazyListState()
    val memoriesScroll = rememberLazyListState()
    val usScroll = rememberLazyListState()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            )
            Scaffold(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                topBar = { CoupleHeader(space.couple.you.name, space.couple.partner.name) },
                bottomBar = {
                    Box {
                        Column {
                            if (navigation.tab == MainTab.WISHES) {
                                PrimaryButton(
                                    stringResource(Res.string.add_wish),
                                    { navigation.open(OverlayRoute.Form()) },
                                    Modifier.padding(horizontal = AppSpacing.page, vertical = AppSpacing.small),
                                    leadingIcon = { Text("+") },
                                )
                            }
                            BottomNavigation(navigation.tab, navigation::select)
                        }
                        if (navigation.tab == MainTab.WISHES && wishesScroll.canScrollForward) {
                            val background = MaterialTheme.colorScheme.background
                            Box(
                                Modifier.fillMaxWidth().height(AppSpacing.section).offset(y = -AppSpacing.section)
                                    .background(
                                        Brush.verticalGradient(listOf(background.copy(alpha = 0f), background)),
                                    ),
                            )
                        }
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
                    verticalArrangement =
                        Arrangement.spacedBy(
                            if (navigation.tab != MainTab.US) AppSpacing.listGap else AppSpacing.detailsGap,
                        ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AppSpacing.large),
                ) {
                    if (navigation.tab == MainTab.WISHES) {
                        wishesContent(
                            space,
                            today,
                            navigation.category,
                            onCategory = { navigation.category = it },
                            sort = navigation.sort,
                            onSort = { navigation.sort = it },
                            onOpen = { navigation.open(OverlayRoute.Details(it)) },
                        )
                    } else if (navigation.tab == MainTab.MEMORIES) {
                        memoriesContent(space) { navigation.open(OverlayRoute.Details(it)) }
                    } else {
                        usContent(space, darkTheme, language, onSignOut) { usDialog = it }
                    }
                }
            }
            usDialog?.let { dialog ->
                UsSettingsDialog(
                    dialog,
                    space.couple.you.name,
                    darkTheme,
                    language,
                    onDismiss = { usDialog = null },
                    onRenameUser = onRenameUser,
                    onSetDarkTheme = onSetDarkTheme,
                    onSetLanguage = onSetLanguage,
                    syncedProfile = onSignOut != null,
                )
            }
            navigation.overlay?.let { route ->
                OverlayDialog(
                    darkTheme = darkTheme,
                    onDismiss = navigation::back,
                ) {
                    Surface(
                        Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(Modifier.safeDrawingPadding().imePadding()) {
                            when (route) {
                                is OverlayRoute.Details -> {
                                    val wish = space.wishes.find { it.id == route.wishId }
                                    if (wish != null) {
                                        WishDetails(
                                            wish,
                                            space.couple,
                                            today,
                                            onBack = {
                                                if (wish.completion != null) {
                                                    navigation.select(
                                                        MainTab.MEMORIES,
                                                    )
                                                } else {
                                                    navigation.back()
                                                }
                                            },
                                            onEdit = { navigation.open(OverlayRoute.Form(wish.id)) },
                                            onDelete = {
                                                onDeleteWish(wish.id)
                                                navigation.back()
                                            },
                                            onRestore = {
                                                onRestoreWish(wish.id)
                                                navigation.back()
                                            },
                                            onEditMemory = { onEditMemory(wish.id, it) },
                                            onComplete = {
                                                onCompleteWish(wish.id)
                                                navigation.back()
                                            },
                                        )
                                    } else {
                                        TextButton(navigation::back) { Text(stringResource(Res.string.back)) }
                                    }
                                }

                                is OverlayRoute.Form -> {
                                    WishForm(
                                        wishId = route.wishId,
                                        initial = space.wishes.find { it.id == route.wishId }?.content,
                                        couple = space.couple,
                                        today = today,
                                        onCancel = navigation::back,
                                        onSave = { draft ->
                                            onSaveWish(route.wishId, draft)
                                            navigation.back()
                                        },
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
                colors =
                    NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                onClick = {
                    onSelect(tab)
                },
                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                icon = {
                    Icon(
                        when (tab) {
                            MainTab.WISHES -> AppIcons.FavoriteBorder
                            MainTab.MEMORIES -> AppIcons.PhotoLibrary
                            MainTab.US -> AppIcons.People
                        },
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
internal fun WishDateBadge(dates: WishDates, today: LocalDate) {
    val status = dates.status(today)
    if (status == DateStatus.NONE) return
    val date =
        when (dates) {
            WishDates.NoDate -> {
                ""
            }

            is WishDates.Deadline -> {
                wishDateText(dates.date)
            }

            is WishDates.Range -> {
                if (dates.start.year == dates.end.year && dates.start.month == dates.end.month) {
                    stringResource(
                        Res.string.same_month_range,
                        dates.start.day,
                        dates.end.day,
                        stringArrayResource(Res.array.short_months)[dates.start.month.ordinal],
                        dates.start.year,
                    )
                } else {
                    stringResource(Res.string.date_range, wishDateText(dates.start), wishDateText(dates.end))
                }
            }
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

@Composable
internal fun wishDateText(date: LocalDate): String =
    stringResource(
        Res.string.formatted_date,
        date.day,
        stringArrayResource(Res.array.short_months)[date.month.ordinal],
        date.year,
    )
