package com.alad1nks.you2.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class DesktopGoogleConfigTest {
    @Test
    fun readsInstalledClientWithoutExposingSecretInDiagnostics() {
        val config =
            DesktopGoogleConfig.parse(
                """{"installed":{"client_id":"desktop-id","client_secret":"desktop-secret"}}""",
            )!!
        assertEquals("desktop-id", config.clientId)
        assertEquals("desktop-secret", config.clientSecret)
        assertFalse(config.toString().contains("desktop-secret"))
        assertNull(DesktopGoogleConfig.parse("""{"installed":{"client_id":"desktop-id"}}""")!!.clientSecret)
    }

    @Test
    fun rejectsWebClientsMissingIdsAndMalformedJson() {
        listOf(
            """{"web":{"client_id":"web-id","client_secret":"web-secret"}}""",
            """{"installed":{"client_id":" "}}""",
            "{}",
            "not json",
        ).forEach { assertNull(DesktopGoogleConfig.parse(it)) }
    }
}
