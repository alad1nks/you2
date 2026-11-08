package com.alad1nks.you2.auth

import org.junit.Assume.assumeTrue
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PlatformCredentialsTest {
    @Test
    fun windowsSessionSurvivesReopeningAndSignOutDeletesIt() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"))
        val service = "com.alad1nks.you2.test.${UUID.randomUUID()}"
        val account = "logout-regression-Қазақ-日本語"
        val store = PlatformCredentials(service, account)
        try {
            assertNull(store.read())
            store.clear()
            store.write("first-test-token")
            assertEquals("first-test-token", PlatformCredentials(service, account).read())
            store.write("rotated-test-token")
            assertEquals("rotated-test-token", store.read())
            store.clear()
            assertNull(store.read())
            store.clear()
            store.write("next-test-token")
            assertEquals("next-test-token", store.read())
            // Previously stored blank markers must not be restored as refresh tokens.
            WindowsCredentials(service, account).write(" ")
            assertNull(store.read())
        } finally {
            store.clear()
        }
    }

    @Test
    fun accessFailuresAreNotMistakenForMissingCredentials() {
        assertFalse(NativeCalls.WinResult(0, 1168).foundOrSucceeded())
        assertFailsWith<IllegalStateException> { NativeCalls.WinResult(0, 5).foundOrSucceeded() }
        assertFailsWith<IllegalStateException> { NativeCalls.WinResult(0, 1168).requireSuccess() }
    }

    @Test
    fun validatesTokensBeforeCallingNativeCode() {
        val store = PlatformCredentials("test", "test")
        assertFailsWith<IllegalArgumentException> { store.write(" ") }
        assertFailsWith<IllegalArgumentException> { store.write("token\u0000suffix") }
        assertFailsWith<IllegalArgumentException> { PlatformCredentials("test\u0000", "test") }
    }
}
