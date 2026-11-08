package com.alad1nks.you2.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/** Bound the whole request, including auth-token acquisition and Firebase's network retries. */
internal suspend fun <T> loadWebImage(timeoutMillis: Long = 15_000, load: suspend () -> T): T? =
    try {
        withTimeout(timeoutMillis) { load() }
    } catch (_: TimeoutCancellationException) {
        reportImageFailure("timeout")
        null
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Throwable) {
        reportImageFailure(error.asDynamic().code as? String ?: error::class.simpleName.orEmpty())
        null
    }

private fun reportImageFailure(code: String) {
    // Do not log private object paths, tokens, or image contents.
    val message = "Wish image could not be loaded ($code). Check the browser Network panel for the failed request."
    js("console.warn(message)")
}
