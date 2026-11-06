package com.alad1nks.you2.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.DemoSpace
import com.alad1nks.you2.core.model.WishCategory
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ImageViewerTest {
    @Test
    fun swipeDownDismissesImage() = exerciseVerticalSwipe(upward = false)

    @Test
    fun swipeUpDismissesImage() = exerciseVerticalSwipe(upward = true)

    private fun exerciseVerticalSwipe(upward: Boolean) =
        runDesktopComposeUiTest(width = 412, height = 917) {
            var open by mutableStateOf(true)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        if (open) {
                            ImageViewer(listOf(WishImage(WishCategory.ADVENTURES)), 0, "Gallery") { open = false }
                        }
                    }
                }
            }
            val direction = if (upward) -1f else 1f
            onNodeWithTag("zoomable-image-0").performTouchInput {
                down(center)
                moveTo(center + Offset(0f, direction * 60f), delayMillis = 200)
                moveTo(center + Offset(0f, direction * 100f), delayMillis = 200)
            }
            onNodeWithTag("image-pages").assert(
                SemanticsMatcher("Image follows the vertical drag") {
                    it.config[ImageDismissOffset] * direction > 70f
                },
            )
            onNodeWithTag("zoomable-image-0").performTouchInput { up() }
            onNodeWithTag("image-pages").assert(SemanticsMatcher.expectValue(ImageDismissOffset, 0f))
            // A short, slow drag settles back instead of closing the viewer.
            onNodeWithTag("zoomable-image-0").performTouchInput {
                swipe(center, center + Offset(0f, direction * 70f), durationMillis = 700)
            }
            onNodeWithTag("image-pages").assert(SemanticsMatcher.expectValue(ImageDismissOffset, 0f))
            // Vertical panning while zoomed must not dismiss the image.
            onNodeWithTag("zoomable-image-0").performTouchInput { doubleClick() }
            onNodeWithTag("zoomable-image-0").performTouchInput {
                if (upward) swipeUp() else swipeDown()
            }
            onNodeWithTag("image-viewer").assertIsDisplayed()
            onNodeWithTag("zoomable-image-0").performTouchInput { doubleClick() }
            // Dismissal also works with hidden controls.
            onNodeWithTag("zoomable-image-0").performTouchInput { click() }
            mainClock.advanceTimeBy(400)
            onNodeWithTag("zoomable-image-0").performTouchInput {
                if (upward) swipeUp() else swipeDown()
            }
            onNodeWithTag("image-viewer").assertDoesNotExist()
        }

    @Test
    fun zoomPanAndToggleControls() =
        runDesktopComposeUiTest(width = 412, height = 917) {
            var open by mutableStateOf(true)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        if (open) {
                            ImageViewer(
                                listOf(WishImage(WishCategory.ADVENTURES), WishImage(WishCategory.PLANS)),
                                0,
                                "Gallery",
                            ) { open = false }
                        }
                    }
                }
            }
            onNodeWithTag("zoomable-image-0").performTouchInput { click() }
            mainClock.advanceTimeBy(400)
            onNodeWithContentDescription("Close image viewer").assertDoesNotExist()
            onNodeWithTag("image-thumbnails").assertDoesNotExist()
            onNodeWithTag("image-viewer-top-shadow").assertDoesNotExist()
            onNodeWithTag("zoomable-image-0").performTouchInput { click() }
            mainClock.advanceTimeBy(400)
            onNodeWithContentDescription("Close image viewer").assertIsDisplayed()
            onNodeWithTag("image-viewer-top-shadow").assertIsDisplayed()
            mainClock.autoAdvance = false
            onNodeWithTag("zoomable-image-0").performTouchInput { doubleClick() }
            mainClock.advanceTimeBy(100)
            onNodeWithTag("zoomable-image-0").assert(
                SemanticsMatcher("Double-tap zoom animates through intermediate scales") {
                    it.config[ImageZoom] > 1f && it.config[ImageZoom] < 3f
                },
            )
            mainClock.advanceTimeBy(300)
            mainClock.autoAdvance = true
            onNodeWithTag("zoomable-image-0").assert(SemanticsMatcher.expectValue(ImageZoom, 3f))
            onNodeWithTag("zoomable-image-0").performTouchInput { swipeLeft() }
            onNodeWithText("1 / 2").assertIsDisplayed()
            onNodeWithTag("zoomable-image-0").performTouchInput { doubleClick() }
            onNodeWithTag("zoomable-image-0").assert(SemanticsMatcher.expectValue(ImageZoom, 1f))
            onNodeWithTag("zoomable-image-0").performTouchInput {
                down(0, center - Offset(30f, 0f))
                down(1, center + Offset(30f, 0f))
                for (step in 1..10) {
                    advanceEventTime(16)
                    moveTo(0, center - Offset(30f + step * 10f, 0f), delayMillis = 0)
                    moveTo(1, center + Offset(30f + step * 10f, 0f), delayMillis = 0)
                }
                up(0)
                up(1)
            }
            onNodeWithTag("zoomable-image-0").assert(
                SemanticsMatcher("Pinch increases scale") {
                    it.config[ImageZoom] > 1f
                },
            )
            onNodeWithTag("image-thumbnail-1").performClick()
            onNodeWithTag("zoomable-image-1").assert(SemanticsMatcher.expectValue(ImageZoom, 1f))
            onNodeWithTag("zoomable-image-1").performTouchInput { click() }
            mainClock.advanceTimeBy(400)
            onNodeWithTag("image-viewer").performKeyInput { pressKey(Key.Escape) }
            onNodeWithTag("image-viewer").assertDoesNotExist()
        }

    @Test
    fun zoomBoundsRespectLandscapeAndPortraitImages() {
        val transform = ImageTransform()
        transform.viewport = Size(400f, 800f)
        transform.imageSize = Size(1000f, 500f)
        transform.change(3f, Offset(10000f, 10000f))
        assertEquals(Offset(400f, 0f), transform.offset)
        transform.change(100f)
        assertEquals(5f, transform.scale)
        transform.change(.01f)
        assertEquals(1f, transform.scale)
        assertEquals(Offset.Zero, transform.offset)
        transform.imageSize = Size(400f, 800f)
        transform.change(3f, anchor = Offset(100f, 200f))
        assertEquals(Offset(-200f, -400f), transform.offset)
        assertTrue(transform.scale == 3f)
    }

    @Test
    fun coverAttachmentsNavigationAndDismissal() = exerciseDetails(completed = false)

    @Test
    fun memoryImagesOpenAndReturnToDetails() = exerciseDetails(completed = true)

    private fun exerciseDetails(completed: Boolean) =
        runDesktopComposeUiTest(width = 412, height = 917) {
            val space = DemoSpace.create()
            val wish =
                (if (completed) space.memories.first() else space.active.first()).let {
                    it.copy(content = it.content.copy(attachments = listOf(WishImage(WishCategory.PLANS))))
                }
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = false) {
                        WishDetails(wish, space.couple, DemoDateSource.today(), {}, {}, {}, {})
                    }
                }
            }
            onNodeWithTag("wish-cover").performClick()
            onNodeWithText("1 / 2").assertIsDisplayed()
            onNodeWithTag("image-thumbnail-0").assertIsSelected()
            onNodeWithTag("image-thumbnail-1").performClick()
            onNodeWithText("2 / 2").assertIsDisplayed()
            onNodeWithTag("image-thumbnail-1").assertIsSelected()
            onNodeWithTag("image-viewer").performKeyInput { pressKey(Key.DirectionLeft) }
            onNodeWithText("1 / 2").assertIsDisplayed()
            onNodeWithTag("image-pages").performTouchInput { swipeLeft() }
            onNodeWithText("2 / 2").assertIsDisplayed()
            onNodeWithTag("image-viewer").performKeyInput { pressKey(Key.DirectionRight) }
            onNodeWithText("2 / 2").assertIsDisplayed()
            onNodeWithContentDescription("Close image viewer").performClick()
            onNodeWithTag("image-viewer").assertDoesNotExist()
            onNodeWithTag("wish-details").performScrollToNode(hasTestTag("wish-attachment-0"))
            onNodeWithTag("wish-attachment-0").performClick()
            onNodeWithText("2 / 2").assertIsDisplayed()
            onNodeWithTag("image-viewer").performKeyInput { pressKey(Key.Escape) }
            onNodeWithTag("image-viewer").assertDoesNotExist()
            onNodeWithTag("wish-attachment-0").assertIsDisplayed()
        }

    @Test
    fun singleImageHidesGalleryChrome() = capture(412, 917, single = true)

    @Test
    fun mobileGallery() = capture(412, 917)

    @Test
    fun desktopGallery() = capture(1280, 900)

    @Test
    fun narrowGalleryWithManyImages() = capture(320, 640, count = 13, initialIndex = 12)

    private fun capture(width: Int, height: Int, single: Boolean = false, count: Int = 2, initialIndex: Int = 0) =
        runDesktopComposeUiTest(width = width, height = height) {
            var open by mutableStateOf(true)
            val images =
                List(if (single) 1 else count) {
                    WishImage(if (it == 0) WishCategory.ADVENTURES else WishCategory.PLANS)
                }
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f), LocalAppLocale provides "en") {
                    AppTheme(darkTheme = true) {
                        if (open) ImageViewer(images, initialIndex, "A weekend by the sea") { open = false }
                    }
                }
            }
            if (single) {
                onNodeWithTag("image-counter").assertDoesNotExist()
                onNodeWithTag("image-thumbnails").assertDoesNotExist()
                onNodeWithText("A weekend by the sea").assertDoesNotExist()
            } else {
                onNodeWithText("${initialIndex + 1} / $count").assertIsDisplayed()
                onNodeWithTag("image-thumbnail-$initialIndex").assertIsDisplayed().assertIsSelected()
            }
            onNodeWithTag("image-viewer").captureRoboImage(
                "src/jvmTest/screenshots/image_viewer_${width}_${images.size}.png",
            )
            onNodeWithContentDescription("Close image viewer").performClick()
            onNodeWithTag("image-viewer").assertDoesNotExist()
        }
}
