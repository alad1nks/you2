package com.alad1nks.you2.ui

import java.awt.image.BufferedImage
import java.io.DataInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Read only JPEG marker headers; no original EXIF data is copied into the upload. */
internal fun desktopExifOrientation(file: File): Int =
    runCatching {
        DataInputStream(file.inputStream().buffered()).use { input ->
            if (input.readUnsignedShort() != 0xffd8) return@use 1
            while (true) {
                if (input.readUnsignedByte() != 0xff) return@use 1
                var marker = input.readUnsignedByte()
                while (marker == 0xff) marker = input.readUnsignedByte()
                if (marker == 0xda || marker == 0xd9) return@use 1
                val size = input.readUnsignedShort() - 2
                require(size >= 0)
                val data = input.readNBytes(size)
                require(data.size == size)
                if (marker != 0xe1 || size < 14 ||
                    !data.copyOfRange(
                        0,
                        6,
                    ).contentEquals(byteArrayOf(69, 120, 105, 102, 0, 0))
                ) {
                    continue
                }
                val tiff = ByteBuffer.wrap(data, 6, size - 6).slice()
                tiff.order(if (tiff.getShort(0).toInt() == 0x4949) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN)
                require(tiff.getShort(2).toInt() == 42)
                val offset = tiff.getInt(4)
                val count = tiff.getShort(offset).toInt() and 0xffff
                repeat(count) { index ->
                    val entry = offset + 2 + index * 12
                    if (tiff.getShort(
                            entry,
                        ).toInt() == 0x112 && tiff.getShort(entry + 2).toInt() == 3 && tiff.getInt(entry + 4) == 1
                    ) {
                        return@use tiff.getShort(entry + 8).toInt().takeIf { it in 1..8 } ?: 1
                    }
                }
                return@use 1
            }
            @Suppress("UNREACHABLE_CODE")
            1
        }
    }.getOrDefault(1)

internal fun orientDesktopImage(image: BufferedImage, orientation: Int): BufferedImage {
    if (orientation !in 2..8) return image
    val width = image.width
    val height = image.height
    val rotated = orientation >= 5
    val result =
        BufferedImage(if (rotated) height else width, if (rotated) width else height, BufferedImage.TYPE_INT_ARGB)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val (dx, dy) =
                when (orientation) {
                    2 -> width - 1 - x to y
                    3 -> width - 1 - x to height - 1 - y
                    4 -> x to height - 1 - y
                    5 -> y to x
                    6 -> height - 1 - y to x
                    7 -> height - 1 - y to width - 1 - x
                    else -> y to width - 1 - x
                }
            result.setRGB(dx, dy, image.getRGB(x, y))
        }
    }
    image.flush()
    return result
}
