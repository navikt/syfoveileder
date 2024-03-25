package no.nav.syfo.veileder.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.GraphApiUser
import no.nav.syfo.testhelper.*
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.configuredJacksonMapper
import no.nav.syfo.veileder.VeilederInfo
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class VeiledereApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    val basePath = "/syfoveileder/api/v3/veiledere"

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

            describe("Get Veilederinfo for self") {
                val urlVeilederinfoSelf = "$basePath/self"

                val validTokenVeileder1 = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = UserConstants.VEILEDER_IDENT,
                )

                describe("Happy path") {

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value[0]
                    val redisCache = externalMockEnvironment.redisCache
                    val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}${UserConstants.VEILEDER_IDENT}"

                    it("should return OK if request is successful and graphapi response should be cached") {
                        redisCache.getObject<GraphApiUser>(cacheKey) shouldBeEqualTo null
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoSelf) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenVeileder1))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederInfo = objectMapper.readValue<VeilederInfo>(response.content!!)

                            veilederInfo.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT
                            veilederInfo.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                            veilederInfo.etternavn shouldBeEqualTo graphapiUserResponse.surname
                            veilederInfo.epost shouldBeEqualTo graphapiUserResponse.mail
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

            describe("Get Veilederinfo for veileder ident") {
                val urlVeilederinfoNotSelf = "$basePath/${UserConstants.VEILEDER_IDENT}"

                val validTokenVeileder2 = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = UserConstants.VEILEDER_IDENT_2,
                )

                describe("Happy path") {

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value[0]

                    it("should return OK if request is successful") {
                        with(
                            handleRequest(HttpMethod.Get, urlVeilederinfoNotSelf) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenVeileder2))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederInfoDTO = objectMapper.readValue<VeilederInfo>(response.content!!)

                            veilederInfoDTO.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT
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

            describe("Get list of Veiledere for enhetNr") {
                val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"

                val validTokenVeileder = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = UserConstants.VEILEDER_IDENT,
                )

                describe("Happy path") {

                    val axsysResponse = externalMockEnvironment.axsysMock.axsysResponse

                    val graphapiUserResponse = externalMockEnvironment.graphApiMock.graphapiUserResponse.value.first()
                    val redisCache = externalMockEnvironment.redisCache
                    val cacheKey = "${AxsysClient.AXSYS_CACHE_KEY_PREFIX}${UserConstants.ENHET_NR}"

                    it("should return OK if request is successful and veilederlist should be cached") {
                        redisCache.getListObject<AxsysVeileder>(cacheKey) shouldBeEqualTo null
                        with(
                            handleRequest(HttpMethod.Get, urlVeiledereEnhetNr) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenVeileder))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val veilederInfoList = objectMapper.readValue<List<VeilederInfo>>(response.content!!)

                            veilederInfoList.size shouldBeEqualTo axsysResponse.size

                            veilederInfoList.first().ident shouldBeEqualTo axsysResponse.first().appIdent
                            veilederInfoList.first().fornavn shouldBeEqualTo graphapiUserResponse.givenName
                            veilederInfoList.first().etternavn shouldBeEqualTo graphapiUserResponse.surname

                            veilederInfoList.last().ident shouldBeEqualTo axsysResponse.last().appIdent
                            veilederInfoList.last().fornavn shouldBeEqualTo ""
                            veilederInfoList.last().etternavn shouldBeEqualTo ""
                            redisCache.getListObject<AxsysVeileder>(cacheKey) shouldNotBeEqualTo null
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
