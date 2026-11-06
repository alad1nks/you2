package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.NativeAuthClient
import com.alad1nks.you2.auth.NativeAuthFailure
import com.alad1nks.you2.auth.NativeAuthProvider
import com.alad1nks.you2.auth.NativeAuthUser
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.WishDocument
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeCloudRepositoryTest {
    private val auth = FakeAuth()
    private val provider = FakeCloud()
    private var today = LocalDate(2026, 11, 5)
    private val repository = NativeCloudRepository(provider, NativeAuthClient(auth), DateSource { today })
    private val draft = WishDraft("A real wish", WishCategory.ADVENTURES)

    @Test
    fun snapshotsReplaceDemoDataAndCarryMemoriesAndMetadata() {
        assertTrue(repository.space.value.wishes.isEmpty())
        assertEquals(CloudPhase.LOADING, repository.cloudState.value.phase)
        val document =
            NativeCloudDocument(
                "wish",
                mapOf(
                    "authorId" to "me",
                    "createdOn" to "2026-11-04",
                    "content" to WishDocument.content(draft),
                    "completion" to mapOf("date" to "2026-11-05", "authorId" to "partner", "memory" to "Our day"),
                ),
            )
        provider.emit(ready().copy(wishes = listOf(document), fromCache = true, pendingWrites = true))
        assertEquals("Our day", repository.space.value.memories.single().completion?.memory)
        assertEquals("Partner", repository.space.value.couple.partner.name)
        assertTrue(repository.cloudState.value.fromCache)
        assertTrue(repository.cloudState.value.pendingWrites)
        provider.emit(ready())
        assertTrue(repository.space.value.wishes.isEmpty())
        assertFalse(repository.cloudState.value.pendingWrites)
    }

    @Test
    fun accountSwitchClearsDataAndIgnoresOldListenersAndWriteCompletions() {
        provider.emit(ready())
        repository.renameUser("New name")
        val oldCompletion = provider.completion!!
        val oldListener = provider.observers.last()
        auth.change("other")
        assertEquals(1, provider.removed)
        assertEquals("other", repository.cloudState.value.userId)
        assertEquals("", repository.space.value.couple.you.id)
        oldListener(ready())
        oldCompletion(NativeCloudFailure.ACCESS)
        assertEquals(CloudPhase.LOADING, repository.cloudState.value.phase)
        assertNull(repository.cloudState.value.failure)
        auth.change(null)
        assertNull(repository.cloudState.value.userId)
        repository.retry()
        assertEquals(1, provider.writes.size)
    }

    @Test
    fun retryPreservesWishIdAndDateButNewOperationsUseCurrentDate() {
        provider.emit(ready())
        val id = repository.create(draft)
        assertEquals("2026-11-05", provider.writes.last().fields["createdOn"])
        provider.completion!!(NativeCloudFailure.NETWORK)
        today = LocalDate(2026, 11, 6)
        repository.retry()
        assertEquals(id, provider.writes.last().id)
        assertEquals(provider.writes.first(), provider.writes.last())
        provider.completion!!(null)
        repository.complete(id)
        assertEquals("2026-11-06", provider.writes.last().fields["date"])
        provider.completion!!(null)
        repository.editMemory(id, "Together")
        assertEquals("memory", provider.writes.last().operation)
        assertEquals("Together", provider.writes.last().fields["memory"])
        provider.completion!!(null)
        repository.restore(id)
        assertEquals("restore", provider.writes.last().operation)
        provider.completion!!(null)
        repository.delete(id)
        assertEquals("delete", provider.writes.last().operation)
    }

    @Test
    fun malformedDataStopsTheSessionAndRetryResubscribes() {
        provider.emit(ready().copy(wishes = listOf(NativeCloudDocument("broken", emptyMap()))))
        assertEquals(CloudFailure.DATA, repository.cloudState.value.failure)
        assertEquals(CloudPhase.ERROR, repository.cloudState.value.phase)
        assertEquals(1, provider.removed)
        provider.emit(ready())
        assertEquals(CloudPhase.ERROR, repository.cloudState.value.phase)
        repository.retry()
        provider.emit(ready())
        assertEquals(CloudPhase.READY, repository.cloudState.value.phase)
        assertNull(repository.cloudState.value.failure)
        repository.close()
        provider.emit(ready())
        auth.change("after-close")
        assertNull(repository.cloudState.value.userId)
        assertTrue(repository.space.value.wishes.isEmpty())
        assertEquals(2, provider.observers.size)
    }

    @Test
    fun invitationsValidateCodesTrimNamesAndKeepRetryIdentifiers() {
        provider.emit(ready().copy(phase = NativeCloudPhase.UNPAIRED, coupleId = null))
        repository.joinInvitation(" Name ", "bad code")
        assertEquals(CloudFailure.INVITE, repository.cloudState.value.failure)
        assertTrue(provider.writes.isEmpty())
        repository.joinInvitation(" Name ", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ")
        assertEquals("a".repeat(32), provider.writes.last().id)
        assertEquals("Name", provider.writes.last().fields["name"])
        provider.completion!!(null)
        repository.createInvitation(" Name ")
        val invitation = provider.writes.last()
        provider.completion!!(NativeCloudFailure.NETWORK)
        repository.retry()
        assertEquals(invitation, provider.writes.last())
    }

    @Test
    fun localPhotosReachNativeUploaderAndSurviveRetryWithoutWeakeningFirestoreValidation() {
        provider.emit(ready())
        val photo = WishImage(source = "file:///cache/photo.jpg")
        val withPhotos = draft.copy(cover = photo, attachments = listOf(photo))
        repository.create(withPhotos)
        val first = provider.writes.last()
        val content = first.fields["content"] as Map<*, *>
        assertEquals(photo.key, content["cover"])
        assertEquals(listOf(photo.key), content["attachments"])
        assertFailsWith<IllegalArgumentException> { WishDocument.content(withPhotos) }
        provider.completion!!(NativeCloudFailure.NETWORK)
        repository.retry()
        assertEquals(first, provider.writes.last())
        provider.completion!!(null)
        repository.edit(first.id, withPhotos)
        assertEquals(content, provider.writes.last().fields["content"])
        provider.completion!!(null)
        assertFailsWith<IllegalArgumentException> {
            repository.edit(first.id, draft.copy(attachments = List(13) { photo }))
        }
    }

    private fun ready() =
        NativeCloudSnapshot(
            NativeCloudPhase.READY,
            "Me",
            "pair",
            null,
            "partner",
            "Partner",
            emptyList(),
            false,
            false,
            null,
        )
}

private class FakeCloud : NativeCloudProvider {
    val observers = mutableListOf<(NativeCloudSnapshot) -> Unit>()
    var removed = 0
    var sequence = 0
    val writes = mutableListOf<Write>()
    var completion: ((NativeCloudFailure?) -> Unit)? = null

    data class Write(
        val operation: String,
        val uid: String,
        val pair: String?,
        val id: String,
        val fields: Map<String, Any?>,
    )

    override fun observe(userId: String, onChange: (NativeCloudSnapshot) -> Unit): () -> Unit {
        observers += onChange
        return { removed++ }
    }

    fun emit(snapshot: NativeCloudSnapshot) = observers.last()(snapshot)

    override fun newId() = (++sequence).toString().padStart(32, '0')

    override fun write(
        operation: String,
        userId: String,
        coupleId: String?,
        documentId: String,
        fields: Map<String, Any?>,
        complete: (NativeCloudFailure?) -> Unit,
    ) {
        writes += Write(operation, userId, coupleId, documentId, fields)
        completion = complete
    }
}

private class FakeAuth : NativeAuthProvider {
    override var currentUser: NativeAuthUser? = NativeAuthUser("me", "me@example.com")
    private var observer: ((NativeAuthUser?) -> Unit)? = null

    override fun observeUser(onChange: (NativeAuthUser?) -> Unit): () -> Unit {
        observer = onChange
        onChange(currentUser)
        return { observer = null }
    }

    fun change(id: String?) {
        currentUser = id?.let { NativeAuthUser(it, "$it@example.com") }
        observer?.invoke(currentUser)
    }

    override fun signIn(email: String, password: String, complete: (NativeAuthFailure?) -> Unit) = Unit

    override fun register(email: String, password: String, complete: (NativeAuthFailure?) -> Unit) = Unit

    override fun resetPassword(email: String, complete: (NativeAuthFailure?) -> Unit) = Unit

    override fun signOut(): NativeAuthFailure? = null
}
