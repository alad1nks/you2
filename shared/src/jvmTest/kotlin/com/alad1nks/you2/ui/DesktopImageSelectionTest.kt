package com.alad1nks.you2.ui

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.util.Base64
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DesktopImageSelectionTest {
    @Test
    fun largeTransparentPngBecomesBoundedOpaqueJpeg() {
        val file = Files.createTempFile("you2-image", ".png").toFile()
        try {
            ImageIO.write(BufferedImage(4096, 1024, BufferedImage.TYPE_INT_ARGB), "png", file)
            val source = desktopImageSelection(file)
            assertTrue(source.startsWith("data:image/jpeg;base64,"))
            val bytes = Base64.getDecoder().decode(source.substringAfter(','))
            assertTrue(bytes.size in 1..5_242_880)
            val decoded = ImageIO.read(ByteArrayInputStream(bytes))
            assertEquals(2048, decoded.width)
            assertEquals(512, decoded.height)
            assertEquals(0xffffff, decoded.getRGB(0, 0) and 0xffffff)
            file.writeText("not an image")
            assertFailsWith<IllegalArgumentException> { desktopImageSelection(file) }
        } finally {
            file.delete()
        }
    }

    @Test
    fun jpegExifRotatesPixelsBeforeMetadataIsRemoved() {
        val file = Files.createTempFile("you2-oriented", ".jpg").toFile()
        try {
            ImageIO.write(BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB), "jpg", file)
            val jpeg = file.readBytes()
            val exif =
                byteArrayOf(
                    69,
                    120,
                    105,
                    102,
                    0,
                    0,
                    73,
                    73,
                    42,
                    0,
                    8,
                    0,
                    0,
                    0,
                    1,
                    0,
                    18,
                    1,
                    3,
                    0,
                    1,
                    0,
                    0,
                    0,
                    6,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                )
            file.writeBytes(
                jpeg.copyOfRange(
                    0,
                    2,
                ) +
                    byteArrayOf(
                        255.toByte(),
                        225.toByte(),
                        0,
                        (exif.size + 2).toByte(),
                    ) + exif + jpeg.copyOfRange(2, jpeg.size),
            )
            assertEquals(6, desktopExifOrientation(file))
            val bytes = Base64.getDecoder().decode(desktopImageSelection(file).substringAfter(','))
            val decoded = ImageIO.read(ByteArrayInputStream(bytes))
            assertEquals(8, decoded.width)
            assertEquals(12, decoded.height)
            assertTrue(!bytes.toString(Charsets.ISO_8859_1).contains("Exif"))
        } finally {
            file.delete()
        }
    }
}
