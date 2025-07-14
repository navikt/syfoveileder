package no.nav.syfo.client.azuread

import com.azure.core.credential.TokenRequestContext
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AzureAdTokenTest {

    @Test
    fun `Endre azureAdToken til tokenCredential`() {
        val expiresAt = LocalDateTime.of(2025, 7, 14, 10, 0)
        val azureAdToken = AzureAdToken("ABC", expiresAt)

        val tokenCredential = azureAdToken.toTokenCredential()
        val accessToken = tokenCredential.getTokenSync(TokenRequestContext())

        assertEquals("ABC", accessToken.token)
        assertEquals(expiresAt, accessToken.expiresAt.toLocalDateTime())
    }
}
