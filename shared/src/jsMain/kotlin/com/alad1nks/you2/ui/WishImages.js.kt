package com.alad1nks.you2.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.alad1nks.you2.auth.firebaseApp
import com.alad1nks.you2.auth.getAuth
import com.alad1nks.you2.auth.onAuthStateChanged
import com.alad1nks.you2.cloud.getBytes
import com.alad1nks.you2.cloud.getStorage
import com.alad1nks.you2.cloud.jpegBytes
import com.alad1nks.you2.cloud.ref
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.image_selection_error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skia.Image
import kotlin.js.Promise

@Composable
internal actual fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    val scope = rememberCoroutineScope()
    val dismiss by rememberUpdatedState(onDismiss)
    val select by rememberUpdatedState(onSelect)
    var failed by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    DisposableEffect(maxImages) {
        var active = true
        val input = js("document.createElement('input')")
        input.type = "file"
        input.accept = "image/*"
        input.multiple = maxImages > 1
        input.style.display = "none"
        js("document.body.appendChild(input)")
        input.oncancel = { if (active) dismiss() }
        input.onchange = {
            scope.launch {
                if (active) {
                    processing = true
                    try {
                        val count = input.files.length as Int
                        require(count <= maxImages)
                        val images = (0 until count).map { WishImage(source = normalizePhoto(input.files[it])) }
                        if (active) {
                            if (images.isEmpty()) dismiss() else select(images)
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Throwable) {
                        if (active) failed = true
                    } finally {
                        if (active) processing = false
                    }
                }
            }
            Unit
        }
        input.click()
        onDispose {
            active = false
            input.onchange = null
            input.oncancel = null
            input.remove()
        }
    }
    if (processing) {
        AlertDialog(onDismissRequest = {}, text = { CircularProgressIndicator() }, confirmButton = {})
    }
    if (failed) {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = { Text(stringResource(Res.string.image_selection_error)) },
            confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

/** Decode and re-encode to strip metadata and bound both dimensions and upload size. */
internal suspend fun normalizePhoto(file: dynamic): String {
    val url = js("URL.createObjectURL(file)") as String
    try {
        val photo = js("new globalThis.Image()")
        photo.src = url
        photo.decode().unsafeCast<Promise<Unit>>().await()
        val width = photo.naturalWidth as Int
        val height = photo.naturalHeight as Int
        require(width > 0 && height > 0)
        val scale = minOf(1.0, 2048.0 / maxOf(width, height))
        val canvas = js("document.createElement('canvas')")
        canvas.width = maxOf(1, (width * scale).toInt())
        canvas.height = maxOf(1, (height * scale).toInt())
        val context = canvas.getContext("2d")
        context.fillStyle = "#ffffff"
        context.fillRect(0, 0, canvas.width, canvas.height)
        context.drawImage(photo, 0, 0, canvas.width, canvas.height)
        val source = canvas.toDataURL("image/jpeg", 0.85) as String
        require(jpegBytes(source).size in 1..5_242_880)
        return source
    } finally {
        js("URL.revokeObjectURL(url)")
    }
}

@Composable
internal actual fun wishImageState(image: WishImage): WishImageState {
    val fallback = painterResource(image.category.image)
    val auth = remember { getAuth(firebaseApp) }
    var uid by remember { mutableStateOf(auth.currentUser?.uid) }
    DisposableEffect(auth) {
        val unsubscribe = onAuthStateChanged(auth) { uid = it?.uid }
        onDispose { unsubscribe() }
    }
    var loaded by remember(image, uid) { mutableStateOf<Painter?>(null) }
    var loading by remember(image, uid) { mutableStateOf(image.source != null) }
    LaunchedEffect(image, uid) {
        val owner = uid
        try {
            val painter =
                loadWebImage {
                    val source = image.source ?: return@loadWebImage null
                    val bytes =
                        if (image.isLocal) {
                            jpegBytes(source)
                        } else {
                            check(owner != null)
                            val buffer = getBytes(ref(getStorage(firebaseApp), source), 5_242_880).await()
                            val array = js("new Int8Array(buffer)")
                            ByteArray(array.length as Int) { (array[it] as Int).toByte() }
                        }
                    BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
                }
            if (auth.currentUser?.uid == owner) loaded = painter
        } finally {
            loading = false
        }
    }
    return WishImageState(loaded ?: fallback, isLoading = loading)
}

internal actual val supportsPhotoSelection: Boolean = true
