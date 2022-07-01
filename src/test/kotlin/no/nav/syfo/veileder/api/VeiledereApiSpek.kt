package no.nav.syfo.veileder.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ENHET_NR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.configuredJacksonMapper
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class VeiledereApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(VeiledereApiSpek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment()

            beforeGroup {
                externalMockEnvironment.startExternalMocks()
            }

            afterGroup {
                externalMockEnvironment.stopExternalMocks()
            }

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )

            describe("Get list of Veiledre for enhetNr=$ENHET_NR") {
                val urlVeiledereEnhetNr = "$apiVeiledereBasePath$apiVeiledereEnhetPath/$ENHET_NR"

                val validTokenVeileder = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = VEILEDER_IDENT,
                )

                describe("Happy path") {

                    val axsysResponse = externalMockEnvironment.axsysMock.axsysResponse

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value.first()

                    it("should return OK if request is successful") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeiledereEnhetNr) {
                                addHeader(Authorization, bearerHeader(validTokenVeileder))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederDTOList = objectMapper.readValue<List<VeilederDTO>>(response.content!!)

                            veilederDTOList.size shouldBeEqualTo axsysResponse.size

                            veilederDTOList.first().ident shouldBeEqualTo axsysResponse.first().appIdent
                            veilederDTOList.first().fornavn shouldBeEqualTo graphapiUserResponse.givenName
                            veilederDTOList.first().etternavn shouldBeEqualTo graphapiUserResponse.surname

                            veilederDTOList.last().ident shouldBeEqualTo axsysResponse.last().appIdent
                            veilederDTOList.last().fornavn shouldBeEqualTo ""
                            veilederDTOList.last().etternavn shouldBeEqualTo ""
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeiledereEnhetNr) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }
                }
            }
        }
    }
})
