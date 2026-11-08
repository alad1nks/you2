package com.alad1nks.you2.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class WebImageLoadingTest {
    @Test
    fun stalledRequestStopsWaiting() =
        MainScope().promise {
            assertNull(loadWebImage(timeoutMillis = 20) { Promise<String> { _, _ -> }.await() })
        }

    @Test
    fun rejectedRequestReturnsFallback() =
        MainScope().promise {
            assertNull(loadWebImage<String> { error("Download denied") })
        }

    @Test
    fun successfulRequestReturnsImage() =
        MainScope().promise {
            assertEquals("image", loadWebImage { Promise.resolve("image").await() })
        }

    @Test
    fun leavingScreenStillCancelsRequest() =
        MainScope().promise {
            assertFailsWith<CancellationException> {
                loadWebImage<String> { throw CancellationException("Screen disposed") }
            }
        }
}
