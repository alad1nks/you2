package com.alad1nks.you2.core.data.repository

import com.alad1nks.you2.core.model.Completion
import com.alad1nks.you2.core.model.Couple
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.core.model.User
import com.alad1nks.you2.core.model.Wish
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlinx.datetime.LocalDate

object DemoDateSource : DateSource {
    override fun today() = LocalDate(2026, 10, 28)
}

/** User-authored demo content deliberately stays unchanged when the UI language changes. */
object DemoSpace {
    fun create(): PairedSpace {
        val titles =
            listOf(
                "A weekend in the mountains",
                "Finally watch About Time",
                "Learn to make pottery",
                "A little gift, just because",
                "A weekend by the sea",
                "Start a new series",
                "A slow Sunday together",
                "Our little wish jar",
                "A picnic at sunset",
                "A book for each other",
                "Try a new recipe",
                "A classic movie marathon",
                "Our first pottery class",
                "A night under the stars",
                "Finally watched About Time",
                "A sunrise by the lake",
            )
        val categories =
            listOf(
                WishCategory.ADVENTURES,
                WishCategory.MOVIES,
                WishCategory.PLANS,
                WishCategory.PRESENTS,
                WishCategory.ADVENTURES,
                WishCategory.TV_SERIES,
                WishCategory.PLANS,
                WishCategory.OTHER,
                WishCategory.ADVENTURES,
                WishCategory.PRESENTS,
                WishCategory.PLANS,
                WishCategory.MOVIES,
                WishCategory.PLANS,
                WishCategory.ADVENTURES,
                WishCategory.MOVIES,
                WishCategory.ADVENTURES,
            )
        val descriptions =
            listOf(
                "A cabin, fresh air, and no alarms.",
                "Blankets, popcorn, phones away.",
                "Two wonky mugs for our morning coffee.",
                "Something small to make you smile.",
                "Slow mornings and long coastal walks.",
                "Save the next episode for each other.",
                "Coffee, a walk, and no plans to rush.",
                "Little ideas for days that need a spark.",
                "Bring our favorite snacks and a blanket.",
                "Choose a story you think I will love.",
                "Make a mess. Make something delicious.",
                "Two films, one very cozy evening.",
                "Learn to make pottery and create two mugs for our morning coffee.",
                "Take a blanket and a flask of tea to watch the stars.",
                "Make time for a film we have both been wanting to see.",
                "Wake up early and watch the sunrise by the lake.",
            )
        val dates =
            listOf(
                WishDates.NoDate,
                WishDates.Deadline(LocalDate(2026, 10, 28)),
                WishDates.Deadline(LocalDate(2026, 11, 16)),
                WishDates.Deadline(LocalDate(2026, 10, 26)),
                WishDates.Range(LocalDate(2026, 11, 14), LocalDate(2026, 11, 16)),
                WishDates.Range(LocalDate(2026, 10, 26), LocalDate(2026, 10, 30)),
                WishDates.Range(LocalDate(2026, 10, 20), LocalDate(2026, 10, 24)),
                WishDates.NoDate,
                WishDates.Deadline(LocalDate(2026, 11, 7)),
                WishDates.NoDate,
                WishDates.Range(LocalDate(2026, 10, 31), LocalDate(2026, 11, 1)),
                WishDates.NoDate,
            )
        return PairedSpace(
            Couple(User("alex", "Alex"), User("mia", "Mia")),
            titles.mapIndexed { index, title ->
                Wish(
                    id = "wish-${16 - index}",
                    authorId = if (index % 2 == 0) "mia" else "alex",
                    createdOn = LocalDate(2026, 9, 28 - index),
                    content =
                        WishDraft(
                            title,
                            categories[index],
                            descriptions.getOrElse(index) { "" },
                            dates.getOrElse(index) { WishDates.NoDate },
                            cover = if (index == 4) WishImage(WishCategory.ADVENTURES) else null,
                            attachments =
                                if (index == 4) {
                                    listOf(
                                        WishImage(WishCategory.PLANS),
                                        WishImage(WishCategory.OTHER),
                                    )
                                } else {
                                    emptyList()
                                },
                        ),
                    completion =
                        if (index >= 12) {
                            Completion(
                                LocalDate(2026, if (index < 14) 10 else 9, listOf(24, 10, 27, 13)[index - 12]),
                                if (index % 2 == 0) "alex" else "mia",
                                listOf(
                                    "Two wonky mugs. A perfect afternoon.",
                                    "Cold hands, hot tea, our favorite sky.",
                                    "We laughed. We cried. We hit replay.",
                                    "Worth every sleepy minute.",
                                )[index - 12],
                            )
                        } else {
                            null
                        },
                )
            },
        )
    }
}
