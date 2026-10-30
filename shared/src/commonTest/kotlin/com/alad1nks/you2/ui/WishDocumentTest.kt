package com.alad1nks.you2.ui

import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.data.repository.WishDocument
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class WishDocumentTest {
    @Test
    fun persistedWishesPreserveDatesImagesAndMemories() {
        DemoSpace.create().wishes.forEach { wish ->
            val document =
                mapOf(
                    "authorId" to wish.authorId,
                    "createdOn" to wish.createdOn.toString(),
                    "content" to WishDocument.content(wish.content),
                    "completion" to
                        wish.completion?.let {
                            mapOf("date" to it.date.toString(), "authorId" to it.authorId, "memory" to it.memory)
                        },
                )
            assertEquals(wish, WishDocument.decode(wish.id, document))
        }
    }

    @Test
    fun malformedDocumentsAreNotSilentlyShownAsEmptyWishes() {
        assertFails { WishDocument.decode("id", emptyMap()) }
        val data =
            mapOf(
                "authorId" to "user",
                "createdOn" to "2026-10-31",
                "content" to (
                    WishDocument.content(WishDraft("A wish", WishCategory.OTHER)) +
                        ("dates" to mapOf("type" to "deadline", "end" to "not-a-date"))
                ),
                "completion" to null,
            )
        assertFails { WishDocument.decode("id", data) }
        assertFails {
            WishDocument.content(
                WishDraft("A wish", WishCategory.OTHER, attachments = List(13) { WishImage(WishCategory.OTHER) }),
            )
        }
    }
}
