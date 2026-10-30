package com.alad1nks.you2.core.model

import kotlinx.datetime.LocalDate

data class User(val id: String, val name: String)

data class Couple(val you: User, val partner: User)

enum class WishCategory { ADVENTURES, PRESENTS, PLANS, MOVIES, TV_SERIES, OTHER }

/** A local demo image key, resolved by the presentation layer. */
data class WishImage(val category: WishCategory)

sealed interface WishDates {
    data object NoDate : WishDates

    data class Deadline(val date: LocalDate) : WishDates

    data class Range(val start: LocalDate, val end: LocalDate) : WishDates {
        init {
            require(end >= start)
        }
    }
}

enum class DateStatus { NONE, UPCOMING_DEADLINE, TODAY, OVERDUE, UPCOMING_RANGE, IN_PROGRESS, ENDED }

fun WishDates.status(today: LocalDate): DateStatus =
    when (this) {
        WishDates.NoDate -> {
            DateStatus.NONE
        }

        is WishDates.Deadline -> {
            when {
                date < today -> DateStatus.OVERDUE
                date == today -> DateStatus.TODAY
                else -> DateStatus.UPCOMING_DEADLINE
            }
        }

        is WishDates.Range -> {
            when {
                end < today -> DateStatus.ENDED
                start > today -> DateStatus.UPCOMING_RANGE
                else -> DateStatus.IN_PROGRESS
            }
        }
    }

fun interface DateSource {
    fun today(): LocalDate
}

data class Completion(val date: LocalDate, val authorId: String, val memory: String = "")

data class WishDraft(
    val title: String,
    val category: WishCategory,
    val description: String = "",
    val dates: WishDates = WishDates.NoDate,
    val cover: WishImage? = null,
    val attachments: List<WishImage> = emptyList(),
) {
    val isValid: Boolean
        get() = title.isNotBlank() && title.length <= 80 && description.length <= 500 && attachments.size <= 12
}

data class Wish(
    val id: String,
    val authorId: String,
    val createdOn: LocalDate,
    val content: WishDraft,
    val completion: Completion? = null,
)

data class PairedSpace(val couple: Couple, val wishes: List<Wish>) {
    val active: List<Wish> get() = wishes.filter { it.completion == null }
    val memories: List<Wish> get() = wishes.filter { it.completion != null }
}
