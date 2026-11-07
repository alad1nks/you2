package com.alad1nks.you2.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.backgroundLight
import com.alad1nks.you2.feature.designsystem.theme.onBackgroundLight
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.image_loading_dots
import com.alad1nks.you2.resources.generated.image_loading_spinner
import com.alad1nks.you2.resources.generated.loading_image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal expect fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit)

@Composable
internal expect fun wishImageState(image: WishImage): WishImageState

internal data class WishImageState(val painter: Painter, val isLoading: Boolean = false)

@Composable
internal fun WishImageContent(
    state: WishImageState,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    imageModifier: Modifier = Modifier,
    showLoadingLabel: Boolean = false,
    viewer: Boolean = false,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            state.painter,
            contentDescription,
            Modifier.matchParentSize().then(imageModifier),
            contentScale = contentScale,
        )
        if (state.isLoading) {
            ImageLoadingPlaceholder(Modifier.matchParentSize(), showLoadingLabel, viewer)
        }
    }
}

@Composable
internal fun ImageLoadingPlaceholder(
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    viewer: Boolean = false,
) {
    val label = stringResource(Res.string.loading_image)
    val foreground = if (viewer) backgroundLight else MaterialTheme.colorScheme.secondary
    Box(
        modifier.background(if (viewer) onBackgroundLight else MaterialTheme.colorScheme.surfaceContainer)
            .testTag("image-loading")
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                if (!showLabel) contentDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (viewer) 16.dp else 10.dp),
        ) {
            if (viewer) {
                val rotation by rememberInfiniteTransition(label = "Image loading").animateFloat(
                    0f,
                    360f,
                    infiniteRepeatable(tween(1000, easing = LinearEasing)),
                    label = "Spinner rotation",
                )
                Image(
                    painterResource(Res.drawable.image_loading_spinner),
                    null,
                    Modifier.size(32.dp).graphicsLayer { rotationZ = rotation },
                )
            } else {
                val dots = painterResource(Res.drawable.image_loading_dots)
                val pulse = rememberInfiniteTransition(label = "Loading dots")
                val opacities =
                    List(3) { index ->
                        pulse.animateFloat(
                            1f,
                            1f,
                            infiniteRepeatable(
                                keyframes {
                                    durationMillis = 1200
                                    1f at 150 + index * 150
                                    .3f at 450 + index * 150
                                    1f at 750 + index * 150
                                },
                            ),
                            label = "Dot $index opacity",
                        )
                    }
                Canvas(Modifier.size(23.dp, 5.dp)) {
                    // Animate each dot while retaining the original Figma artwork.
                    opacities.forEachIndexed { index, opacity ->
                        val left = index * 9.dp.toPx()
                        clipRect(left = left, right = left + 5.dp.toPx()) {
                            with(dots) {
                                draw(size, alpha = opacity.value, colorFilter = ColorFilter.tint(foreground))
                            }
                        }
                    }
                }
            }
            if (showLabel) {
                Text(label, color = foreground, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
internal fun WishCover(
    image: WishImage,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    showLoadingLabel: Boolean = true,
) {
    WishCover(wishImageState(image), modifier, height, showLoadingLabel)
}

@Composable
internal fun WishCover(
    state: WishImageState,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    showLoadingLabel: Boolean = true,
) {
    WishImageContent(
        state,
        null,
        modifier.fillMaxWidth().heightIn(min = height, max = height).clip(AppShapes.image),
        contentScale = ContentScale.Crop,
        showLoadingLabel = showLoadingLabel,
    )
}

internal expect val supportsPhotoSelection: Boolean
