package no.nav.syfo.veileder.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.mock.veilederUser
import no.nav.syfo.util.configure
import no.nav.syfo.veileder.VeilederInfo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VeiledereSystemApiTest {

    private lateinit var externalMockEnvironment: ExternalMockEnvironment

    @BeforeEach
    fun setup() {
        externalMockEnvironment = ExternalMockEnvironment()
        externalMockEnvironment.startExternalMocks()
    }

    @AfterEach
    fun tearDown() {
        externalMockEnvironment.stopExternalMocks()
    }

    private fun ApplicationTestBuilder.setupApiAndClient(): HttpClient {
        application {
            testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )
        }
        val client = createClient {
            install(ContentNegotiation) {
                jackson { configure() }
            }
        }

        return client
    }

    @Nested
    inner class `Get Veilederinfo` {
        private val apiUrl = "$systemApiBasePath/veiledere/${UserConstants.VEILEDER_IDENT}"
        
        private fun getValidSystemToken() = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            azp = syfooversiktsrvClientId,
        )

        @Test
        fun `Returns OK if request is successful`() {
            testApplication {
                val client = setupApiAndClient()
                val response = client.get(apiUrl) {
                    bearerAuth(getValidSystemToken())
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val veilederInfo = response.body<VeilederInfo>()

                assertEquals(UserConstants.VEILEDER_IDENT, veilederInfo.ident)
                assertEquals(veilederUser.givenName, veilederInfo.fornavn)
                assertEquals(veilederUser.surname, veilederInfo.etternavn)
                assertEquals(veilederUser.mail, veilederInfo.epost)
                assertEquals(true, veilederInfo.enabled)
            }
        }

        @Test
        fun `Returns status Forbidden when wrong consumer azp`() {
            val invalidValidSystemToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = "invalid_azp",
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.get(apiUrl) {
                    bearerAuth(invalidValidSystemToken)
                }

                assertEquals(HttpStatusCode.Forbidden, response.status)
            }
        }
    }
}
