package com.alad1nks.you2.ui

import android.view.View
import android.view.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ViewerSystemBarsTest {
    @Test
    fun dialogBarsFollowControlsVisibility() =
        runComposeUiTest {
            val hidden = mutableStateOf(false)
            lateinit var view: View
            setContent {
                OverlayDialog(darkTheme = true, onDismiss = {}, hideSystemBars = hidden.value) {
                    view = LocalView.current
                    Box(Modifier.fillMaxSize())
                }
            }
            runOnIdle {
                assertTrue(view.rootWindowInsets.isVisible(WindowInsets.Type.statusBars()))
                assertTrue(view.rootWindowInsets.isVisible(WindowInsets.Type.navigationBars()))
                hidden.value = true
            }
            runOnIdle {
                assertFalse(view.rootWindowInsets.isVisible(WindowInsets.Type.statusBars()))
                assertFalse(view.rootWindowInsets.isVisible(WindowInsets.Type.navigationBars()))
                hidden.value = false
            }
            runOnIdle {
                assertTrue(view.rootWindowInsets.isVisible(WindowInsets.Type.statusBars()))
                assertTrue(view.rootWindowInsets.isVisible(WindowInsets.Type.navigationBars()))
            }
        }
}
