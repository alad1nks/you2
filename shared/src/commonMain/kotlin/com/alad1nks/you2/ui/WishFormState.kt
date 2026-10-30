package com.alad1nks.you2.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlinx.datetime.LocalDate

internal enum class WishDateMode { NONE, DEADLINE, RANGE }

/** An isolated, saveable edit session. Repository changes happen only on Save. */
internal class WishFormState(initial: WishDraft? = null) {
    var title by mutableStateOf(initial?.title.orEmpty())
    var category by mutableStateOf(initial?.category ?: WishCategory.OTHER)
    var description by mutableStateOf(initial?.description.orEmpty())
    var cover by mutableStateOf(initial?.cover)
    var attachments by mutableStateOf(initial?.attachments.orEmpty().toList())
    var mode by mutableStateOf(
        when (initial?.dates) {
            is WishDates.Deadline -> WishDateMode.DEADLINE
            is WishDates.Range -> WishDateMode.RANGE
            else -> WishDateMode.NONE
        },
    )
        private set
    var start by mutableStateOf(
        when (val dates = initial?.dates) {
            is WishDates.Deadline -> dates.date
            is WishDates.Range -> dates.start
            else -> null
        },
    )
    var end by mutableStateOf((initial?.dates as? WishDates.Range)?.end)

    fun selectMode(value: WishDateMode) {
        mode = value
        if (value == WishDateMode.NONE) start = null
        if (value != WishDateMode.RANGE) end = null
    }

    val datesInvalid: Boolean
        get() =
            mode != WishDateMode.NONE &&
                (start == null || (mode == WishDateMode.RANGE && (end == null || end!! < start!!)))

    fun draftOrNull(): WishDraft? {
        if (datesInvalid) return null
        return WishDraft(
            title = title,
            category = category,
            description = description,
            dates =
                when (mode) {
                    WishDateMode.NONE -> WishDates.NoDate
                    WishDateMode.DEADLINE -> WishDates.Deadline(requireNotNull(start))
                    WishDateMode.RANGE -> WishDates.Range(requireNotNull(start), requireNotNull(end))
                },
            cover = cover,
            attachments = attachments.toList(),
        ).takeIf { it.isValid }
    }

    companion object {
        val Saver =
            listSaver<WishFormState, String>(
                save = {
                    listOf(
                        it.title,
                        it.category.name,
                        it.description,
                        it.cover?.category?.name.orEmpty(),
                        it.mode.name,
                        it.start?.toString().orEmpty(),
                        it.end?.toString().orEmpty(),
                    ) + it.attachments.map { image -> image.category.name }
                },
                restore = {
                    WishFormState().apply {
                        title = it[0]
                        category = WishCategory.valueOf(it[1])
                        description = it[2]
                        cover = it[3].takeIf(String::isNotEmpty)?.let { key -> WishImage(WishCategory.valueOf(key)) }
                        mode = WishDateMode.valueOf(it[4])
                        start = it[5].takeIf(String::isNotEmpty)?.let(LocalDate::parse)
                        end = it[6].takeIf(String::isNotEmpty)?.let(LocalDate::parse)
                        attachments = it.drop(7).map { key -> WishImage(WishCategory.valueOf(key)) }
                    }
                },
            )
    }
}
