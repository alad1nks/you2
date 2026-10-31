package com.alad1nks.you2.core.data.repository

import com.alad1nks.you2.core.model.Completion
import com.alad1nks.you2.core.model.Wish
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDates
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlinx.datetime.LocalDate

/** Firestore's wire format stays outside the domain models. */
object WishDocument {
    fun content(draft: WishDraft): Map<String, Any?> {
        require(draft.isValid && draft.attachments.size <= 12)
        require(draft.cover?.isLocal != true && draft.attachments.none { it.isLocal })
        val dates = draft.dates
        return mapOf(
            "title" to draft.title,
            "category" to draft.category.name,
            "description" to draft.description,
            "dates" to
                when (dates) {
                    WishDates.NoDate -> {
                        mapOf("type" to "none")
                    }

                    is WishDates.Deadline -> {
                        mapOf("type" to "deadline", "end" to dates.date.toString())
                    }

                    is WishDates.Range -> {
                        mapOf(
                            "type" to "range",
                            "start" to dates.start.toString(),
                            "end" to dates.end.toString(),
                        )
                    }
                },
            "cover" to draft.cover?.key,
            "attachments" to draft.attachments.map { it.key },
        )
    }

    fun decode(id: String, data: Map<String, Any?>): Wish {
        val content = data["content"] as Map<*, *>
        val dates = content["dates"] as Map<*, *>
        val draft =
            WishDraft(
                title = content["title"] as String,
                category = WishCategory.valueOf(content["category"] as String),
                description = content["description"] as String,
                dates =
                    when (dates["type"]) {
                        "none" -> {
                            WishDates.NoDate
                        }

                        "deadline" -> {
                            WishDates.Deadline(LocalDate.parse(dates["end"] as String))
                        }

                        "range" -> {
                            WishDates.Range(
                                LocalDate.parse(dates["start"] as String),
                                LocalDate.parse(dates["end"] as String),
                            )
                        }

                        else -> {
                            error("Unknown date type")
                        }
                    },
                cover = (content["cover"] as String?)?.let { WishImage.fromKey(it) },
                attachments = (content["attachments"] as List<*>).map { WishImage.fromKey(it as String) },
            )
        require(draft.isValid && draft.attachments.size <= 12)
        require(draft.cover?.isLocal != true && draft.attachments.none { it.isLocal })
        val completion =
            (data["completion"] as Map<*, *>?)?.let {
                Completion(LocalDate.parse(it["date"] as String), it["authorId"] as String, it["memory"] as String)
            }
        require(completion == null || completion.memory.length <= 500)
        return Wish(id, data["authorId"] as String, LocalDate.parse(data["createdOn"] as String), draft, completion)
    }
}
