package com.alad1nks.you2.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.image_selection_error
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skia.Image
import org.koin.compose.koinInject
import platform.Foundation.NSData
import platform.posix.memcpy

@Composable
internal actual fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    val provider = koinInject<NativePhotoProvider>()
    val dismiss by rememberUpdatedState(onDismiss)
    val select by rememberUpdatedState(onSelect)
    var failed by remember { mutableStateOf(false) }
    DisposableEffect(provider, maxImages) {
        val cancel =
            provider.pick(maxImages) { paths ->
                when {
                    paths == null -> failed = true
                    paths.isEmpty() -> dismiss()
                    else -> select(paths.take(maxImages).map { WishImage(source = it) })
                }
            }
        onDispose { cancel() }
    }
    if (failed) {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = { Text(stringResource(Res.string.image_selection_error)) },
            confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
internal actual fun wishImageState(image: WishImage): WishImageState {
    val provider = koinInject<NativePhotoProvider>()
    val fallback = painterResource(image.category.image)
    var loaded by remember(image) { mutableStateOf<Painter?>(null) }
    var loading by remember(image) { mutableStateOf(image.source != null) }
    DisposableEffect(provider, image) {
        val cancel =
            image.source?.let { source ->
                provider.load(source) { bytes ->
                    loaded =
                        bytes?.let {
                            runCatching {
                                BitmapPainter(
                                    Image.makeFromEncoded(it.toByteArray()).toComposeImageBitmap(),
                                )
                            }.getOrNull()
                        }
                    loading = false
                }
            }
        onDispose { cancel?.invoke() }
    }
    return WishImageState(loaded ?: fallback, isLoading = loading)
}

internal actual val supportsPhotoSelection: Boolean = true

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).also { result ->
        if (result.isNotEmpty()) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    }
