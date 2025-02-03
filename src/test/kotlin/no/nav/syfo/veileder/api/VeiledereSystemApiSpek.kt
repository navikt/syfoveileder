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
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class VeiledereSystemApiSpek : Spek({
    describe(VeiledereSystemApiSpek::class.java.simpleName) {

        val externalMockEnvironment = ExternalMockEnvironment()

        fun ApplicationTestBuilder.setupApiAndClient(): HttpClient {
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

        beforeGroup {
            externalMockEnvironment.startExternalMocks()
        }

        afterGroup {
            externalMockEnvironment.stopExternalMocks()
        }

        describe("Get Veilederinfo") {
            val apiUrl = "$systemApiBasePath/veiledere/${UserConstants.VEILEDER_IDENT}"
            val validSystemToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = syfooversiktsrvClientId,
            )

            it("Returns OK if request is successful") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(apiUrl) {
                        bearerAuth(validSystemToken)
                    }

                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val veilederInfo = response.body<VeilederInfo>()

                    veilederInfo.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT
                    veilederInfo.fornavn shouldBeEqualTo veilederUser.givenName
                    veilederInfo.etternavn shouldBeEqualTo veilederUser.surname
                    veilederInfo.epost shouldBeEqualTo veilederUser.mail
                    veilederInfo.enabled shouldBeEqualTo true
                }
            }

            it("Returns status Forbidden when wrong consumer azp") {
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

                    response.status shouldBeEqualTo HttpStatusCode.Forbidden
                }
            }
        }
    }
})
