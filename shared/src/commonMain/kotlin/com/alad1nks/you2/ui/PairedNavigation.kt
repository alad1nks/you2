package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.alad1nks.you2.core.model.WishCategory

enum class MainTab { WISHES, MEMORIES, US }

sealed interface OverlayRoute {
    data class Details(val wishId: String) : OverlayRoute

    data class Form(val wishId: String? = null) : OverlayRoute
}

@Stable
class PairedNavigation {
    var tab by mutableStateOf(MainTab.WISHES)
        private set
    var sort by mutableStateOf(WishSort.NEWEST)
    var category by mutableStateOf<WishCategory?>(null)
    var overlay by mutableStateOf<OverlayRoute?>(null)
        private set

    fun select(tab: MainTab) {
        this.tab = tab
        overlay = null
    }

    fun open(route: OverlayRoute) {
        overlay = route
    }

    fun back() {
        val route = overlay
        overlay = if (route is OverlayRoute.Form && route.wishId != null) OverlayRoute.Details(route.wishId) else null
    }

    companion object {
        val Saver =
            listSaver<PairedNavigation, String>(
                save = {
                    listOf(
                        it.tab.name,
                        it.category?.name.orEmpty(),
                        when (it.overlay) {
                            is OverlayRoute.Details -> "details"
                            is OverlayRoute.Form -> "form"
                            null -> ""
                        },
                        when (val route = it.overlay) {
                            is OverlayRoute.Details -> route.wishId
                            is OverlayRoute.Form -> route.wishId.orEmpty()
                            null -> ""
                        },
                        it.sort.name,
                    )
                },
                restore = { values ->
                    PairedNavigation().apply {
                        select(MainTab.valueOf(values[0]))
                        sort = values.getOrNull(4)?.let(WishSort::valueOf) ?: WishSort.NEWEST
                        category = values[1].takeIf { it.isNotEmpty() }?.let(WishCategory::valueOf)
                        when (values[2]) {
                            "details" -> open(OverlayRoute.Details(values[3]))
                            "form" -> open(OverlayRoute.Form(values[3].takeIf { it.isNotEmpty() }))
                        }
                    }
                },
            )
    }
}

@Composable
internal fun rememberPairedNavigation(initialTab: MainTab = MainTab.WISHES) =
    rememberSaveable(saver = PairedNavigation.Saver) { PairedNavigation().apply { select(initialTab) } }
