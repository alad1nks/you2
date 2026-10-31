package com.alad1nks.you2.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.alad1nks.you2.core.model.WishImage
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.image_selection_error
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.util.UUID

@Composable
internal actual fun PlatformImagePicker(maxImages: Int, onDismiss: () -> Unit, onSelect: (List<WishImage>) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var failed by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var launched by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val receive: (List<Uri>) -> Unit = { uris ->
        if (uris.isEmpty()) {
            onDismiss()
        } else {
            scope.launch {
                processing = true
                val files = mutableListOf<File>()
                try {
                    val images =
                        withContext(Dispatchers.IO) {
                            require(uris.size <= maxImages)
                            uris.map { uri ->
                                val bitmap =
                                    ImageDecoder.decodeBitmap(
                                        ImageDecoder.createSource(context.contentResolver, uri),
                                    ) { decoder, info, _ ->
                                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                        val scale = minOf(1.0, 2048.0 / maxOf(info.size.width, info.size.height))
                                        decoder.setTargetSize(
                                            maxOf(1, (info.size.width * scale).toInt()),
                                            maxOf(1, (info.size.height * scale).toInt()),
                                        )
                                    }
                                val directory = File(context.cacheDir, "wish-images").apply { mkdirs() }
                                val file = File(directory, "${UUID.randomUUID()}.jpg")
                                files += file
                                try {
                                    file.outputStream().use {
                                        check(
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it),
                                        )
                                    }
                                } finally {
                                    bitmap.recycle()
                                }
                                require(file.length() in 1..5_242_880)
                                WishImage(source = Uri.fromFile(file).toString())
                            }
                        }
                    onSelect(images)
                } catch (_: Exception) {
                    files.forEach { it.delete() }
                    failed = true
                } finally {
                    processing = false
                }
            }
        }
    }
    val single =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { receive(listOfNotNull(it)) }
    val multiple =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxOf(2, maxImages)),
        ) { receive(it) }
    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (maxImages == 1) single.launch(request) else multiple.launch(request)
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

@Composable
internal actual fun wishImagePainter(image: WishImage): Painter {
    val fallback = painterResource(image.category.image)
    var loaded by remember(image.key) { mutableStateOf<Painter?>(null) }
    LaunchedEffect(image.key) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        loaded =
            try {
                withContext(Dispatchers.IO) {
                    val source = image.source ?: return@withContext null
                    val bytes =
                        if (image.isLocal) {
                            File(requireNotNull(Uri.parse(source).path)).readBytes()
                        } else {
                            Tasks.await(FirebaseStorage.getInstance().reference.child(source).getBytes(5_242_880))
                        }
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    require(options.outWidth > 0 && options.outHeight > 0)
                    options.inSampleSize = 1
                    while (maxOf(
                            options.outWidth,
                            options.outHeight,
                        ) / options.inSampleSize > 1024
                        ) options.inSampleSize *= 2
                    options.inJustDecodeBounds = false
                    BitmapPainter(
                        requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)).asImageBitmap(),
                    )
                }.takeIf { FirebaseAuth.getInstance().currentUser?.uid == uid }
            } catch (_: Exception) {
                null
            }
    }
    return loaded ?: fallback
}

internal actual val supportsPhotoSelection: Boolean = true
