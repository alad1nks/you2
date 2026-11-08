package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.AuthUser
import com.alad1nks.you2.auth.initializeApp
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.WishDocument
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishDraft
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.ui.normalizePhoto
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Run through the Firestore emulator; never connects to the production Firebase project. */
class WebCloudProviderTest {
    @Test
    fun twoAccountsSyncPairingWishesAndMemoriesAndRejectOutsiders() =
        MainScope().promise {
            val alice = Client()
            val bob = Client()
            val outsider = Client()
            val clients = listOf(alice, bob, outsider)
            try {
                clients.forEach { it.waitFor { cloudState.value.phase == CloudPhase.UNPAIRED } }
                assertTrue(alice.repository.space.value.wishes.isEmpty())
                alice.repository.createInvitation("Alice")
                alice.idle()
                alice.waitFor { cloudState.value.inviteCode != null }
                val oldCode = alice.repository.cloudState.value.inviteCode!!
                assertTrue(Regex("[a-f0-9]{32}").matches(oldCode))
                alice.repository.createInvitation("Alice")
                alice.idle()
                alice.waitFor { cloudState.value.inviteCode != oldCode }
                bob.repository.joinInvitation("Bob", oldCode)
                bob.idle(CloudFailure.INVITE)
                bob.repository.joinInvitation("Bob", alice.repository.cloudState.value.inviteCode!!)
                bob.idle()
                alice.waitFor { cloudState.value.phase == CloudPhase.READY }
                bob.waitFor { cloudState.value.phase == CloudPhase.READY }
                assertEquals("Alice", bob.repository.space.value.couple.partner.name)
                assertEquals("Bob", alice.repository.space.value.couple.partner.name)
                val profile = getDoc(doc(alice.db, "users/${alice.uid}")).await()
                val pair = profile.data().coupleId as String

                val draft = WishDraft("A shared web wish", WishCategory.ADVENTURES)
                val id = alice.repository.create(draft)
                alice.idle()
                bob.waitFor { space.value.wishes.any { it.id == id } }
                assertEquals(LocalDate(2026, 11, 7), bob.repository.space.value.wishes.single().createdOn)
                bob.repository.edit(id, draft.copy(title = "Edited by partner"))
                bob.idle()
                alice.waitFor { space.value.wishes.single().content.title == "Edited by partner" }
                bob.repository.complete(id)
                bob.idle()
                alice.waitFor { space.value.memories.size == 1 }
                alice.repository.editMemory(id, "Together")
                alice.idle()
                bob.waitFor { space.value.memories.single().completion?.memory == "Together" }
                // A later completion and a retried create must preserve the first completion and edits.
                alice.repository.complete(id)
                alice.idle()
                assertNull(
                    alice.write(
                        "create",
                        pair,
                        id,
                        mapOf("createdOn" to "2026-11-07", "content" to WishDocument.content(draft)),
                    ),
                )
                val storedSnapshot = getDoc(doc(alice.db, "couples/$pair/wishes/$id")).await()
                val stored = storedSnapshot.data()
                assertEquals(bob.uid, stored.completion.authorId as String)
                assertEquals("Together", stored.completion.memory as String)
                assertEquals("Edited by partner", stored.content.title as String)
                assertNotNull(stored.createdAt)
                assertNotNull(stored.updatedAt)

                val denied =
                    getDoc(doc(outsider.db, "couples/$pair/wishes/$id")).then<Boolean>(
                        { _: dynamic -> false },
                        { error -> webCloudFailure(error) == NativeCloudFailure.ACCESS },
                    ).await()
                assertTrue(denied)
                assertEquals(NativeCloudFailure.ACCESS, outsider.write("delete", pair, id))

                alice.repository.renameUser("Alicia")
                alice.idle()
                bob.waitFor { space.value.couple.partner.name == "Alicia" }
                bob.repository.restore(id)
                bob.idle()
                alice.waitFor { space.value.active.size == 1 }
                assertNull(alice.repository.space.value.wishes.single().completion)

                // Reconnecting starts with server data, not seeded demo content.
                bob.repository.close()
                bob.repository = NativeCloudRepository(bob.provider, bob.auth, dates)
                bob.waitFor { cloudState.value.phase == CloudPhase.READY && space.value.active.size == 1 }
                bob.repository.delete(id)
                bob.idle()
                alice.waitFor { space.value.wishes.isEmpty() }
                alice.auth.change(null)
                assertNull(alice.repository.cloudState.value.userId)
                assertEquals("", alice.repository.space.value.couple.you.id)
                assertTrue(alice.repository.space.value.wishes.isEmpty())
            } finally {
                clients.forEach {
                    it.repository.close()
                    terminate(it.db).await()
                }
            }
        }

    @Test
    fun offlineProfileCacheMissDoesNotCreateOrPublishAnAccount() =
        MainScope().promise {
            val client = Client(start = false)
            try {
                disableNetwork(client.db).await()
                client.start()
                delay(300)
                assertEquals(CloudPhase.LOADING, client.repository.cloudState.value.phase)
                assertFalse(client.repository.cloudState.value.busy)
                enableNetwork(client.db).await()
                client.waitFor { cloudState.value.phase == CloudPhase.UNPAIRED }
                val profile = getDoc(doc(client.db, "users/${client.uid}")).await()
                assertTrue(profile.exists() as Boolean)
                assertEquals("", profile.data().name as String)
                assertNull(profile.data().coupleId)
            } finally {
                client.repository.close()
                terminate(client.db).await()
            }
        }

    @Test
    fun photosUploadPrivatelyReplaceAndDeleteAndCreateRetryPreservesEdits() =
        MainScope().promise {
            val alice = Client()
            val bob = Client()
            val outsider = Client()
            val clients = listOf(alice, bob, outsider)
            try {
                clients.forEach { it.waitFor { cloudState.value.phase == CloudPhase.UNPAIRED } }
                alice.repository.createInvitation("Alice")
                alice.idle()
                alice.waitFor { cloudState.value.inviteCode != null }
                bob.repository.joinInvitation("Bob", alice.repository.cloudState.value.inviteCode!!)
                bob.idle()
                alice.waitFor { cloudState.value.phase == CloudPhase.READY }
                bob.waitFor { cloudState.value.phase == CloudPhase.READY }
                val profile = getDoc(doc(alice.db, "users/${alice.uid}")).await()
                val pair = profile.data().coupleId as String
                val canvas = js("document.createElement('canvas')")
                canvas.width = 4096
                canvas.height = 64
                canvas.getContext("2d").fillRect(0, 0, 4096, 64)
                val data = canvas.toDataURL("image/png") as String
                val response = js("fetch(data)").unsafeCast<Promise<dynamic>>().await()
                val source = normalizePhoto(response.blob().unsafeCast<Promise<dynamic>>().await())
                val photo = js("new Image()")
                photo.src = source
                photo.decode().unsafeCast<Promise<Unit>>().await()
                assertEquals(2048, photo.naturalWidth as Int)
                assertEquals(32, photo.naturalHeight as Int)
                val selection = WishImage(source = source)
                assertTrue(selection.isLocal)
                assertEquals(selection, WishImage.fromKey(selection.key))
                val draft = WishDraft("Photo", WishCategory.OTHER, cover = selection, attachments = listOf(selection))
                val id = alice.repository.create(draft)
                alice.idle()
                bob.waitFor { space.value.wishes.any { it.id == id && it.content.cover?.source != null } }
                val saved = bob.repository.space.value.wishes.single().content
                val path = saved.cover!!.source!!
                assertTrue(path.startsWith("couples/$pair/wishes/$id/"))
                assertEquals(saved.cover, saved.attachments.single())
                val bytes = getBytes(ref(bob.storage, path), 5_242_880).await()
                assertEquals(jpegBytes(source).size, bytes.byteLength as Int)
                val metadata = getMetadata(ref(alice.storage, path)).await()
                assertEquals("image/jpeg", metadata.contentType as String)
                assertTrue(
                    getBytes(ref(outsider.storage, path), 5_242_880).then(
                        { _: dynamic -> false },
                        { error -> error.asDynamic().code == "storage/unauthorized" },
                    ).await(),
                )

                bob.repository.edit(
                    id,
                    saved.copy(title = "Partner edit", cover = selection, attachments = emptyList()),
                )
                bob.idle()
                alice.waitFor { space.value.wishes.single().content.title == "Partner edit" }
                val replacement = alice.repository.space.value.wishes.single().content.cover!!.source!!
                assertTrue(path != replacement)
                assertTrue(
                    getMetadata(ref(alice.storage, path)).then(
                        { _: dynamic -> false },
                        { error -> error.asDynamic().code == "storage/object-not-found" },
                    ).await(),
                )
                val retryContent =
                    WishDocument.content(draft.copy(cover = null, attachments = emptyList())) +
                        mapOf("cover" to source, "attachments" to listOf(source))
                assertNull(
                    alice.write("create", pair, id, mapOf("createdOn" to "2026-11-07", "content" to retryContent)),
                )
                val retried = getDoc(doc(alice.db, "couples/$pair/wishes/$id")).await()
                assertEquals("Partner edit", retried.data().content.title as String)
                alice.repository.delete(id)
                alice.idle()
                // Once the wish is gone, member reads are denied even for missing objects.
                // The emulator-only admin endpoint lets us verify actual object deletion.
                val deletedUrl =
                    "http://127.0.0.1:9199/v0/b/demo-you2.appspot.com/o/" +
                        (js("encodeURIComponent(replacement)") as String)
                val deleted =
                    js("fetch(deletedUrl, {headers: {Authorization: 'Bearer owner'}})")
                        .unsafeCast<Promise<dynamic>>().await()
                assertEquals(404, deleted.status as Int)
            } finally {
                clients.forEach {
                    it.repository.close()
                    terminate(it.db).await()
                }
            }
        }

    private class Client(start: Boolean = true) {
        val uid = js("globalThis.crypto.randomUUID()") as String
        val auth = TestAuth(uid)
        val db: dynamic =
            getFirestore(
                initializeApp(
                    webObject(
                        mapOf(
                            "projectId" to "demo-you2",
                            "apiKey" to "demo-key",
                            "storageBucket" to "demo-you2.appspot.com",
                        ),
                    ),
                    uid,
                ),
            )
        val storage: dynamic = getStorage(db.app)
        val provider: WebCloudProvider
        lateinit var repository: NativeCloudRepository

        init {
            connectFirestoreEmulator(db, "127.0.0.1", 8185, webObject(mapOf("mockUserToken" to mapOf("sub" to uid))))
            connectStorageEmulator(storage, "127.0.0.1", 9199, webObject(mapOf("mockUserToken" to mapOf("sub" to uid))))
            provider = WebCloudProvider(auth, db)
            if (start) start()
        }

        fun start() {
            repository = NativeCloudRepository(provider, auth, dates)
        }

        suspend fun waitFor(condition: NativeCloudRepository.() -> Boolean) {
            withTimeout(15_000) {
                while (!repository.condition()) {
                    check(
                        repository.cloudState.value.phase != CloudPhase.ERROR,
                    ) { repository.cloudState.value.toString() }
                    delay(20)
                }
            }
        }

        suspend fun idle(failure: CloudFailure? = null) {
            waitFor { !cloudState.value.busy }
            assertEquals(failure, repository.cloudState.value.failure)
        }

        suspend fun write(
            operation: String,
            pair: String,
            id: String,
            fields: Map<String, Any?> = emptyMap(),
        ): NativeCloudFailure? =
            suspendCoroutine {
                continuation ->
                provider.write(operation, uid, pair, id, fields) { continuation.resume(it) }
            }
    }

    companion object {
        private val dates = DateSource { LocalDate(2026, 11, 7) }
    }
}

private class TestAuth(uid: String) : AuthClient {
    override var currentUser: AuthUser? = AuthUser(uid, "$uid@example.com")
    private val observers = mutableListOf<(AuthUser?) -> Unit>()

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit {
        observers += onChange
        onChange(currentUser)
        return { observers -= onChange }
    }

    fun change(uid: String?) {
        currentUser = uid?.let { AuthUser(it, "$it@example.com") }
        observers.toList().forEach { it(currentUser) }
    }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) = Unit

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) = Unit

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) = Unit

    override fun signOut(complete: (AuthError?) -> Unit) = Unit
}
