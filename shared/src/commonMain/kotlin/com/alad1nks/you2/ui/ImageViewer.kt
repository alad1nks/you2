package com.alad1nks.you2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.feature.designsystem.theme.backgroundLight
import com.alad1nks.you2.feature.designsystem.theme.onBackgroundLight
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.close_image_viewer
import com.alad1nks.you2.resources.generated.view_image
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImageViewer(images: List<WishImage>, initialIndex: Int, title: String, onDismiss: () -> Unit) {
    if (images.isEmpty()) return
    var controlsVisible by remember { mutableStateOf(true) }
    OverlayDialog(darkTheme = true, onDismiss = onDismiss, hideSystemBars = !controlsVisible) {
        val pager = rememberPagerState(initialPage = initialIndex.coerceIn(images.indices)) { images.size }
        val thumbnails = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val transform = remember(pager.currentPage) { ImageTransform() }
        var zoomAnimation by remember(transform) { mutableStateOf<Job?>(null) }
        DisposableEffect(transform) { onDispose { zoomAnimation?.cancel() } }
        val viewerFocus = remember { FocusRequester() }
        val shadowVisibility = remember { MutableTransitionState(false) }
        shadowVisibility.targetState = controlsVisible
        LaunchedEffect(controlsVisible) { viewerFocus.requestFocus() }
        LaunchedEffect(pager.currentPage) {
            if (images.size > 1) thumbnails.animateScrollToItem(pager.currentPage)
        }
        Box(
            Modifier.fillMaxSize().background(onBackgroundLight).testTag("image-viewer")
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) {
                        false
                    } else {
                        when (event.key) {
                            Key.Escape -> {
                                onDismiss()
                                true
                            }

                            Key.DirectionLeft, Key.DirectionRight -> {
                                val delta = if (event.key == Key.DirectionRight) 1 else -1
                                scope.launch {
                                    pager.scrollToPage((pager.currentPage + delta).coerceIn(images.indices))
                                }
                                true
                            }

                            else -> {
                                false
                            }
                        }
                    }
                }.focusRequester(viewerFocus).focusable(),
        ) {
            HorizontalPager(
                pager,
                Modifier.fillMaxSize().testTag("image-pages"),
                userScrollEnabled = transform.scale == 1f,
            ) { index ->
                val imageState = wishImageState(images[index])
                val painter = imageState.painter
                val active = index == pager.currentPage
                var viewport by remember { mutableStateOf(Size.Zero) }
                val gestures =
                    rememberTransformableState { zoom, pan, _ ->
                        zoomAnimation?.cancel()
                        transform.change(zoom, pan)
                    }
                Box(
                    Modifier.fillMaxSize().clipToBounds()
                        .onSizeChanged {
                            viewport = Size(it.width.toFloat(), it.height.toFloat())
                        }
                        .semantics { this[ImageZoom] = if (active) transform.scale else 1f }
                        .testTag("zoomable-image-$index")
                        .pointerInput(transform) {
                            detectTapGestures(
                                onPress = { zoomAnimation?.cancel() },
                                onTap = { controlsVisible = !controlsVisible },
                                onDoubleTap = { position ->
                                    zoomAnimation?.cancel()
                                    zoomAnimation =
                                        scope.launch {
                                            transform.animateZoom(
                                                position - Offset(size.width / 2f, size.height / 2f),
                                            )
                                        }
                                },
                            )
                        }
                        .transformable(gestures, canPan = { transform.scale > 1f }),
                ) {
                    LaunchedEffect(painter.intrinsicSize, active, transform, viewport) {
                        if (active) {
                            transform.imageSize = painter.intrinsicSize
                            transform.viewport = viewport
                            transform.change(1f)
                        }
                    }
                    WishImageContent(
                        imageState,
                        stringResource(Res.string.view_image, index + 1),
                        Modifier.fillMaxSize(),
                        imageModifier =
                            Modifier.graphicsLayer {
                                if (active) {
                                    scaleX = transform.scale
                                    scaleY = transform.scale
                                    translationX = transform.offset.x
                                    translationY = transform.offset.y
                                }
                            },
                        contentScale = ContentScale.Fit,
                        showLoadingLabel = true,
                        viewer = true,
                    )
                }
            }
            AnimatedVisibility(
                visibleState = shadowVisibility,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(220)),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(144.dp).testTag("image-viewer-top-shadow")
                        .background(imageViewerTopShadow),
                )
            }
            if (controlsVisible) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Row(
                        Modifier.fillMaxWidth().height(72.dp).padding(horizontal = AppSpacing.large),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val closeLabel = stringResource(Res.string.close_image_viewer)
                        Box(
                            Modifier.offset(y = (-4).dp).size(44.dp).clip(AppShapes.pill)
                                .clickable(role = Role.Button, onClick = onDismiss)
                                .semantics { contentDescription = closeLabel },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "×",
                                color = backgroundLight,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 28.sp, lineHeight = 32.sp),
                            )
                        }
                        if (images.size > 1) {
                            Text(
                                "${pager.currentPage + 1} / ${images.size}",
                                Modifier.testTag("image-counter"),
                                color = backgroundLight,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    if (images.size > 1) {
                        Column(
                            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(152.dp)
                                .padding(horizontal = AppSpacing.page, vertical = AppSpacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
                        ) {
                            Text(
                                title,
                                Modifier.fillMaxWidth(),
                                color = backgroundLight,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            LazyRow(
                                state = thumbnails,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
                                modifier = Modifier.testTag("image-thumbnails"),
                            ) {
                                itemsIndexed(images) { index, image ->
                                    val selected = pager.currentPage == index
                                    val shape = RoundedCornerShape(8.dp)
                                    WishImageContent(
                                        wishImageState(image),
                                        stringResource(Res.string.view_image, index + 1),
                                        Modifier.size(64.dp, 48.dp).clip(shape).alpha(if (selected) 1f else .6f)
                                            .then(
                                                if (selected) {
                                                    Modifier.border(
                                                        2.dp,
                                                        backgroundLight,
                                                        shape,
                                                    )
                                                } else {
                                                    Modifier
                                                },
                                            )
                                            .selectable(selected, role = Role.Tab) {
                                                scope.launch { pager.scrollToPage(index) }
                                            }.testTag("image-thumbnail-$index"),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal val ImageZoom = SemanticsPropertyKey<Float>("ImageZoom")

// Ease the opacity slope to zero at both ends instead of joining two linear ramps.
// Squaring the position retains the darker lower portion without a visible seam.
private val imageViewerTopShadow =
    Brush.verticalGradient(
        List(33) { index ->
            val position = index / 32f
            val t = position * position
            val opacity = .95f * (1f - t * t * (3f - 2f * t))
            Color.Black.copy(alpha = opacity.coerceIn(0f, 1f))
        },
    )

/** Translation is bounded by the fitted image, so letterboxing cannot be dragged off screen. */
internal class ImageTransform {
    var scale by mutableStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var viewport = Size.Zero
    var imageSize = Size.Unspecified

    suspend fun animateZoom(anchor: Offset) {
        val startScale = scale
        val startOffset = offset
        val target =
            ImageTransform().also {
                it.viewport = viewport
                it.imageSize = imageSize
                it.scale = scale
                it.offset = offset
                it.change(if (scale > 1f) 1f / scale else 3f, anchor = anchor)
            }
        animate(0f, 1f, animationSpec = tween(250)) { fraction, _ ->
            scale = startScale + (target.scale - startScale) * fraction
            offset = startOffset + (target.offset - startOffset) * fraction
        }
    }

    fun change(zoom: Float, pan: Offset = Offset.Zero, anchor: Offset = Offset.Zero) {
        val next = (scale * zoom).coerceIn(1f, 5f)
        val translated = (offset - anchor) * (next / scale) + anchor + pan
        val source =
            if (imageSize.width.isFinite() && imageSize.height.isFinite() &&
                imageSize.width > 0f && imageSize.height > 0f
            ) {
                imageSize
            } else {
                viewport
            }
        val fit =
            if (source.width > 0f && source.height > 0f) {
                minOf(viewport.width / source.width, viewport.height / source.height)
            } else {
                1f
            }
        val maxX = ((source.width * fit * next - viewport.width) / 2f).coerceAtLeast(0f)
        val maxY = ((source.height * fit * next - viewport.height) / 2f).coerceAtLeast(0f)
        scale = next
        offset =
            if (next == 1f) {
                Offset.Zero
            } else {
                Offset(
                    translated.x.coerceIn(-maxX, maxX),
                    translated.y.coerceIn(-maxY, maxY),
                )
            }
    }
}
