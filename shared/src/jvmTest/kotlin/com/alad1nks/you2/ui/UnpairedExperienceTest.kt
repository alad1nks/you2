package com.alad1nks.you2.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.CloudState
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UnpairedExperienceTest {
    @Test
    fun desktopPairingUsesPersistentNavigation() =
        runDesktopComposeUiTest(width = 1280, height = 900) {
            val repository = PairingCloud()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) { CloudSpaceGate(repository, "user", {}) { Text("Private wishes") } }
                }
            }
            onNodeWithTag("desktop-navigation").assertIsDisplayed()
            onRoot().captureRoboImage("src/jvmTest/screenshots/desktop_unpaired_wishes.png")
            onNodeWithText("Invite your partner").performClick()
            runOnIdle { repository.cloudState.value = repository.cloudState.value.copy(inviteCode = CODE) }
            onNodeWithText(CODE).assertIsDisplayed()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/desktop_pair_invite.png")
            onNodeWithText("Close").performClick()
            onNode(hasText("Memories") and isSelectable()).performClick()
            onRoot().captureRoboImage("src/jvmTest/screenshots/desktop_unpaired_memories.png")
        }

    @Test
    fun creatingInvitationDisablesJoiningOnEveryTab() = invitationOwnerCannotJoin(restored = false)

    @Test
    fun restoredInvitationDisablesJoiningOnEveryTab() = invitationOwnerCannotJoin(restored = true)

    private fun invitationOwnerCannotJoin(restored: Boolean) =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val repository = PairingCloud()
            if (restored) repository.cloudState.value = repository.cloudState.value.copy(inviteCode = CODE)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) { CloudSpaceGate(repository, "user", {}) { Text("Private wishes") } }
                }
            }
            if (!restored) {
                onNodeWithText("I have an invite code").assertIsEnabled()
                onNodeWithText("Invite your partner").performClick()
                runOnIdle { repository.cloudState.value = repository.cloudState.value.copy(inviteCode = CODE) }
                onNodeWithText("Close").performClick()
            }
            for (tab in listOf("Wishes", "Memories", "Us")) {
                onNode(hasText(tab) and isSelectable()).performClick()
                onNodeWithText("I have an invite code").performScrollTo().assertIsNotEnabled().performClick()
                onNodeWithText("You’ve already created an invitation. Share your code with your partner to connect.")
                    .performScrollTo().assertIsDisplayed()
                onNodeWithTag("pair-code").assertDoesNotExist()
                assertEquals(null, repository.joined)
                if (restored && tab == "Wishes") {
                    onRoot().performMouseInput { moveTo(Offset.Zero) }
                    mainClock.advanceTimeBy(500)
                    onRoot().captureRoboImage("src/jvmTest/screenshots/unpaired_invitation_owner.png")
                }
                onNodeWithText("Invite your partner").performScrollTo().performClick()
                onNodeWithText(CODE).assertIsDisplayed()
                onNodeWithText("Create a new invitation").assertIsEnabled()
                onNodeWithText("Close").performClick()
            }
        }

    @Test
    fun usShowsPersonalSpaceAndAllTabsWithoutCreatingAnInvitation() =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val repository = PairingCloud()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) {
                        CloudSpaceGate(
                            repository,
                            "user",
                            {
                            },
                            tabState =
                                rememberSaveable {
                                    mutableStateOf(
                                        MainTab.US,
                                    )
                                },
                            language = Language.ENGLISH,
                        ) { Text("Private wishes") }
                    }
                }
            }
            onNodeWithText("Your space").assertIsDisplayed()
            onNodeWithText("Alex").assertIsDisplayed()
            onNodeWithText("Not paired yet").assertIsDisplayed()
            onNodeWithTag("pair-code").assertDoesNotExist()
            onNodeWithText("Private wishes").assertDoesNotExist()
            onRoot().performMouseInput { moveTo(Offset.Zero) }
            mainClock.advanceTimeBy(500)
            onRoot().captureRoboImage("src/jvmTest/screenshots/unpaired_us.png")
            for (tab in listOf("Wishes", "Memories")) {
                onNode(hasText(tab) and isSelectable()).performClick()
                onNodeWithText("Invite your partner").assertIsDisplayed()
                onNodeWithText("I have an invite code").assertIsDisplayed()
                onRoot().performMouseInput { moveTo(Offset.Zero) }
                mainClock.advanceTimeBy(500)
                onRoot().captureRoboImage("src/jvmTest/screenshots/unpaired_${tab.lowercase()}.png")
            }
            assertEquals(0, repository.creations)
        }

    @Test
    fun inviteSurvivesLoadingCopiesAndWaitsForRealtimePairing() =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val repository = PairingCloud()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) {
                        CloudSpaceGate(repository, "user", {
                        }, tabState = rememberSaveable { mutableStateOf(MainTab.US) }) { Text("Private wishes") }
                    }
                }
            }
            onNodeWithText("Invite your partner").performClick()
            assertEquals(1, repository.creations)
            runOnIdle {
                repository.cloudState.value = repository.cloudState.value.copy(phase = CloudPhase.LOADING, busy = true)
            }
            onNodeWithText("Copy code").assertIsNotEnabled()
            runOnIdle {
                repository.cloudState.value =
                    repository.cloudState.value.copy(
                        phase = CloudPhase.UNPAIRED,
                        busy = false,
                        inviteCode = CODE,
                    )
            }
            onNodeWithText(CODE).assertIsDisplayed()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/pair_invite.png")
            onNodeWithText("Copy code").performClick()
            onNodeWithText("✓ Code copied · Waiting for your partner").assertIsDisplayed()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/pair_copied.png")
            onNodeWithText("Close").performClick()
            onNodeWithText("Your space").assertIsDisplayed()
            onNodeWithText("Invite your partner").performClick()
            assertEquals(1, repository.creations)
            onNodeWithText("Create a new invitation").performClick()
            assertEquals(2, repository.creations)
            runOnIdle { repository.cloudState.value = repository.cloudState.value.copy(phase = CloudPhase.READY) }
            onNodeWithText("You’re connected").assertIsDisplayed()
            onNodeWithText("Private wishes").assertDoesNotExist()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/pair_connected.png")
            onNodeWithText("View our wishes").performClick()
            onNodeWithText("Private wishes").assertIsDisplayed()
        }

    @Test
    fun joinValidationRetainsCodeAndAllowsCorrectionAndCancel() =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val repository = PairingCloud()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(false) {
                        CloudSpaceGate(repository, "user", {
                        }, tabState = rememberSaveable { mutableStateOf(MainTab.US) }) { Text("Private wishes") }
                    }
                }
            }
            onNodeWithText("I have an invite code").performClick()
            onNodeWithText("Join partner").assertIsNotEnabled()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/pair_join.png")
            onNodeWithTag("pair-code").performTextInput(CODE)
            onNodeWithText("Join partner").performClick()
            assertEquals("Alex" to CODE, repository.joined)
            runOnIdle {
                repository.cloudState.value = repository.cloudState.value.copy(failure = CloudFailure.INVITE)
            }
            onNodeWithText("This code is invalid, expired, or already used. Ask your partner for a new one.")
                .assertIsDisplayed()
            onNodeWithTag("pair-dialog").captureRoboImage("src/jvmTest/screenshots/pair_invalid.png")
            onNodeWithText("Cancel").performClick()
            onNodeWithText("Your space").assertIsDisplayed()
            assertEquals(null, repository.cloudState.value.failure)
        }

    @Test
    fun smallPhoneLargeTextKeepsActionsAndSettingsReachable() =
        runDesktopComposeUiTest(width = 320, height = 640) {
            val repository = PairingCloud()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f), LocalAppLocale provides "en") {
                    AppTheme(true) {
                        CloudSpaceGate(repository, "user", {
                        }, tabState = rememberSaveable { mutableStateOf(MainTab.US) }) { Text("Private wishes") }
                    }
                }
            }
            onNodeWithText("Sign out").performScrollTo().performClick()
            onNodeWithTag("sign-out-dialog").assertIsDisplayed()
            onNodeWithTag("sign-out-cancel").performScrollTo().performClick()
            onNodeWithText("Your profile").performScrollTo().performClick()
            onNodeWithTag("profile-name").assertExists()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("I have an invite code").performScrollTo().performClick()
            onNodeWithTag("pair-code").performScrollTo().performTextInput(CODE)
            onNodeWithText("Join partner").performScrollTo().performClick()
            assertEquals("Alex" to CODE, repository.joined)
        }

    @Test
    fun unnamedAccountUsesLocalizedProfileAndInvitationErrorsCanRetry() =
        runDesktopComposeUiTest(width = 412, height = 853) {
            val repository = PairingCloud()
            repository.cloudState.value = repository.cloudState.value.copy(displayName = "")
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "ru") {
                    AppTheme(true) {
                        CloudSpaceGate(
                            repository,
                            "user",
                            {
                            },
                            tabState =
                                rememberSaveable {
                                    mutableStateOf(
                                        MainTab.US,
                                    )
                                },
                            language = Language.RUSSIAN,
                        ) { Text("Private wishes") }
                    }
                }
            }
            onNodeWithText("Ваше пространство").assertIsDisplayed()
            onNodeWithText("Вы").assertIsDisplayed()
            onNodeWithText("Пригласить партнёра").performClick()
            assertEquals("Вы", repository.invitedName)
            runOnIdle {
                repository.cloudState.value = repository.cloudState.value.copy(failure = CloudFailure.NETWORK)
            }
            onNodeWithText("Повторить").performClick()
            assertEquals(1, repository.retries)
        }

    private class PairingCloud :
        CloudWishesRepository,
        WishesRepository by InMemoryWishesRepository(DemoDateSource) {
        override val cloudState = MutableStateFlow(CloudState("user", "Alex", CloudPhase.UNPAIRED))
        var creations = 0
        var invitedName: String? = null
        var retries = 0
        var joined: Pair<String, String>? = null

        override fun createInvitation(name: String) {
            creations++
            invitedName = name
        }

        override fun joinInvitation(name: String, code: String) {
            joined = name to code
        }

        override fun retry() {
            retries++
        }

        override fun dismissFailure() {
            cloudState.value = cloudState.value.copy(failure = null)
        }

        override fun close() = Unit
    }

    companion object {
        private const val CODE = "8f24c0a163b94e72a506d8cf910b3e45"
    }
}
