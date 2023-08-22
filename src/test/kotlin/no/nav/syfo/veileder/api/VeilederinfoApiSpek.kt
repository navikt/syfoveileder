package no.nav.syfo.veileder.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.GraphApiUser
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_2
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.configuredJacksonMapper
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class VeilederinfoApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(VeilederinfoApiSpek::class.java.simpleName) {

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

            describe("Get Veilederinfo for self") {
                val urlVeilederinfoSelf = "$apiVeilederinfoBasePath$apiVeilederinfoSelfPath"

                val validTokenVeileder1 = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = VEILEDER_IDENT,
                )

                describe("Happy path") {

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value[0]
                    val redisCache = externalMockEnvironment.redisCache
                    val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}$VEILEDER_IDENT"

                    it("should return OK if request is successful and graphapi response should be cached") {
                        redisCache.getObject<GraphApiUser>(cacheKey) shouldBeEqualTo null
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoSelf) {
                                addHeader(Authorization, bearerHeader(validTokenVeileder1))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederInfoDTO = objectMapper.readValue<VeilederInfoDTO>(response.content!!)

                            veilederInfoDTO.ident shouldBeEqualTo VEILEDER_IDENT
                            veilederInfoDTO.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                            veilederInfoDTO.etternavn shouldBeEqualTo graphapiUserResponse.surname
                            veilederInfoDTO.epost shouldBeEqualTo graphapiUserResponse.mail
                            redisCache.getObject<GraphApiUser>(cacheKey) shouldNotBeEqualTo null
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoSelf) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }
                }
            }

            describe("Get Veilederinfo for not self") {
                val urlVeilederinfoNotSelf = "$apiVeilederinfoBasePath/$VEILEDER_IDENT"

                val validTokenVeileder2 = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = VEILEDER_IDENT_2,
                )

                describe("Happy path") {

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value[0]

                    it("should return OK if request is successful") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoNotSelf) {
                                addHeader(Authorization, bearerHeader(validTokenVeileder2))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederInfoDTO = objectMapper.readValue<VeilederInfoDTO>(response.content!!)

                            veilederInfoDTO.ident shouldBeEqualTo VEILEDER_IDENT
                            veilederInfoDTO.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                            veilederInfoDTO.etternavn shouldBeEqualTo graphapiUserResponse.surname
                            veilederInfoDTO.epost shouldBeEqualTo graphapiUserResponse.mail
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoNotSelf) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }
                }
            }
        }
    }
})
