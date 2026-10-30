package com.alad1nks.you2.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.CloudState
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CloudSpaceGateTest {
    @Test
    fun previousAccountsReadyDataIsNeverRendered() =
        runDesktopComposeUiTest {
            val repository = FakeCloud()
            repository.cloudState.value = CloudState(userId = "old", phase = CloudPhase.READY)
            setContent {
                CompositionLocalProvider(LocalAppLocale provides "en") {
                    AppTheme(false) { CloudSpaceGate(repository, "new", {}) { Text("Private wishes") } }
                }
            }
            onNodeWithText("Private wishes").assertDoesNotExist()
            onNodeWithText("Loading your space…").assertExists()
            runOnIdle { repository.cloudState.value = CloudState(userId = "new", phase = CloudPhase.READY) }
            onNodeWithText("Private wishes").assertExists()
        }

    @Test
    fun loadFailureOffersRetryAndSignOutWithoutDemoFallback() =
        runDesktopComposeUiTest {
            val repository = FakeCloud()
            repository.cloudState.value =
                CloudState(
                    userId = "user",
                    phase = CloudPhase.ERROR,
                    failure = CloudFailure.ACCESS,
                )
            var signOuts = 0
            setContent {
                CompositionLocalProvider(LocalAppLocale provides "en") {
                    AppTheme(false) { CloudSpaceGate(repository, "user", { signOuts++ }) { Text("Private wishes") } }
                }
            }
            onNodeWithText("Private wishes").assertDoesNotExist()
            onNodeWithText("Retry").performClick()
            onNodeWithText("Sign out").performClick()
            runOnIdle {
                assertEquals(1, repository.retries)
                assertEquals(1, signOuts)
            }
        }

    private class FakeCloud :
        CloudWishesRepository,
        WishesRepository by InMemoryWishesRepository(DemoDateSource) {
        override val cloudState = MutableStateFlow(CloudState())
        var retries = 0

        override fun createInvitation(name: String) = Unit

        override fun joinInvitation(name: String, code: String) = Unit

        override fun retry() {
            retries++
        }

        override fun dismissFailure() = Unit

        override fun close() = Unit
    }
}
