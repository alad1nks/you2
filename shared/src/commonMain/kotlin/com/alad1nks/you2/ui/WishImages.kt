package com.alad1nks.you2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.feature.designsystem.theme.AppShapes

@Composable
internal expect fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit)

@Composable
internal expect fun wishImagePainter(image: WishImage): Painter

@Composable
internal fun WishCover(image: WishImage, modifier: Modifier = Modifier, height: Dp = 160.dp) {
    Image(
        wishImagePainter(image),
        null,
        modifier.fillMaxWidth().heightIn(min = height, max = height).clip(AppShapes.image),
        contentScale = ContentScale.Crop,
    )
}

internal expect val supportsPhotoSelection: Boolean
