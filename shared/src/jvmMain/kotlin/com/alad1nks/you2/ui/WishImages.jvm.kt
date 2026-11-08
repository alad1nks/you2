package com.alad1nks.you2.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.DesktopAuthClient
import com.alad1nks.you2.cloud.DesktopStorageApi
import com.alad1nks.you2.cloud.MAX_IMAGE_BYTES
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.image_selection_error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

internal val LocalDesktopImageFiles =
    androidx.compose.runtime.staticCompositionLocalOf<(Int) -> List<File>> {
        { maxImages ->
            val picker =
                JFileChooser().apply {
                    isMultiSelectionEnabled = maxImages > 1
                    fileFilter = FileNameExtensionFilter("Images (JPEG, PNG)", "jpg", "jpeg", "png")
                    isAcceptAllFileFilterUsed = false
                }
            if (picker.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                if (maxImages > 1) picker.selectedFiles.toList() else listOf(picker.selectedFile)
            } else {
                emptyList()
            }
        }
    }

@Composable
internal actual fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    val selectFiles = LocalDesktopImageFiles.current
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val files =
                withContext(Dispatchers.Main) {
                    selectFiles(maxImages)
                }
            if (files.isEmpty()) {
                onDismiss()
            } else {
                require(files.size <= maxImages)
                val images =
                    withContext(Dispatchers.IO) {
                        files.map { WishImage(source = desktopImageSelection(it)) }
                    }
                onSelect(images)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failed = true
        }
    }
    if (failed) {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = { Text(stringResource(Res.string.image_selection_error)) },
            confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
        )
    } else {
        AlertDialog(onDismissRequest = {}, text = { CircularProgressIndicator() }, confirmButton = {})
    }
}

/** Decode at a bounded size, flatten transparency and strip original metadata before staging. */
internal fun desktopImageSelection(file: File): String {
    val decoded =
        ImageIO.createImageInputStream(file).use { input ->
            requireNotNull(input)
            val readers = ImageIO.getImageReaders(input)
            require(readers.hasNext())
            val reader = readers.next()
            try {
                reader.input = input
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                require(width > 0 && height > 0)
                val sample = maxOf(1, (maxOf(width, height) + 2047) / 2048)
                reader.read(0, reader.defaultReadParam.apply { setSourceSubsampling(sample, sample, 0, 0) })
            } finally {
                reader.dispose()
            }
        }
    val image = orientDesktopImage(decoded, desktopExifOrientation(file))
    val rgb = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val graphics = rgb.createGraphics()
    try {
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, rgb.width, rgb.height)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.drawImage(image, 0, 0, null)
    } finally {
        graphics.dispose()
        image.flush()
    }
    val bytes =
        ByteArrayOutputStream().use { output ->
            try {
                check(ImageIO.write(rgb, "jpg", output))
                output.toByteArray()
            } finally {
                rgb.flush()
            }
        }
    require(bytes.size in 1..MAX_IMAGE_BYTES)
    return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes)
}

@Composable
internal actual fun wishImageState(image: WishImage): WishImageState {
    val fallback = painterResource(image.category.image)
    if (image.source == null) return WishImageState(fallback)
    val auth = if (image.isLocal) null else getKoin().getOrNull<AuthClient>() as? DesktopAuthClient
    var uid by remember(auth) { mutableStateOf(auth?.currentUser?.id) }
    androidx.compose.runtime.DisposableEffect(auth) {
        val unsubscribe = auth?.observeUser { uid = it?.id }
        onDispose { unsubscribe?.invoke() }
    }
    var loaded by remember(image, uid) { mutableStateOf<Painter?>(null) }
    var loading by remember(image, uid) { mutableStateOf(true) }
    LaunchedEffect(image, uid) {
        loaded =
            try {
                val source = requireNotNull(image.source)
                val token = if (!image.isLocal) requireNotNull(auth).tokenFor(requireNotNull(uid)) else null
                withContext(Dispatchers.IO) {
                    val bytes =
                        if (source.startsWith("data:image/jpeg;base64,")) {
                            Base64.getDecoder().decode(source.substringAfter(','))
                        } else {
                            DesktopStorageApi().download(requireNotNull(token), source)
                        }
                    require(bytes.size in 1..MAX_IMAGE_BYTES)
                    val decoded = org.jetbrains.skia.Image.makeFromEncoded(bytes)
                    require(decoded.width.toLong() * decoded.height <= 16_777_216)
                    BitmapPainter(decoded.toComposeImageBitmap())
                }.takeIf { image.isLocal || auth?.currentUser?.id == uid }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        loading = false
    }
    return WishImageState(loaded ?: fallback, loading)
}

internal actual val supportsPhotoSelection: Boolean = true
