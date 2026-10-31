package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable
import com.alad1nks.you2.core.model.WishImage
import org.jetbrains.compose.resources.painterResource

@Composable
internal actual fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    DemoImagePicker(multiple = maxImages > 1, onDismiss = onDismiss) { onSelect(it.take(maxImages)) }
}

@Composable
internal actual fun wishImageState(image: WishImage): WishImageState =
    WishImageState(painterResource(image.category.image))

internal actual val supportsPhotoSelection: Boolean = false
