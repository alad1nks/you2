package com.alad1nks.you2.ui

import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates

enum class WishSort { NEWEST, OLDEST, SOONEST }

/** Stable sorting retains repository order for ties; undated wishes follow dated wishes. */
internal fun PairedSpace.visibleWishes(category: WishCategory?, sort: WishSort = WishSort.NEWEST) =
    active.filter { category == null || it.content.category == category }.let { wishes ->
        when (sort) {
            WishSort.NEWEST -> {
                wishes.sortedByDescending { it.createdOn }
            }

            WishSort.OLDEST -> {
                wishes.sortedBy { it.createdOn }
            }

            WishSort.SOONEST -> {
                wishes.sortedWith(
                    compareBy({ it.content.dates == WishDates.NoDate }, {
                        when (val dates = it.content.dates) {
                            WishDates.NoDate -> null
                            is WishDates.Deadline -> dates.date
                            is WishDates.Range -> dates.start
                        }
                    }),
                )
            }
        }
    }
