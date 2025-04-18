package no.nav.syfo.veileder.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.GraphApiUser
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.mock.generateAxsysResponse
import no.nav.syfo.testhelper.mock.graphapiUserResponse
import no.nav.syfo.util.configure
import no.nav.syfo.veileder.VeilederInfo
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class VeiledereApiSpek : Spek({
    val basePath = "/syfoveileder/api/v3/veiledere"

    describe(VeiledereApiSpek::class.java.simpleName) {

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

        describe("Get Veilederinfo for self") {
            val urlVeilederinfoSelf = "$basePath/self"

            val validTokenVeileder1 = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                navIdent = UserConstants.VEILEDER_IDENT,
            )

            describe("Happy path") {

                val graphapiUserResponse = graphapiUserResponse.value[0]
                val redisCache = externalMockEnvironment.redisCache
                val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}${UserConstants.VEILEDER_IDENT}"

                it("should return OK if request is successful and graphapi response should be cached") {
                    redisCache.getObject<GraphApiUser>(cacheKey) shouldBeEqualTo null
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeilederinfoSelf) {
                            bearerAuth(validTokenVeileder1)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val veilederInfo = response.body<VeilederInfo>()

                        veilederInfo.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT
                        veilederInfo.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                        veilederInfo.etternavn shouldBeEqualTo graphapiUserResponse.surname
                        veilederInfo.epost shouldBeEqualTo graphapiUserResponse.mail
                        veilederInfo.enabled shouldBeEqualTo true
                        redisCache.getObject<GraphApiUser>(cacheKey) shouldNotBeEqualTo null
                    }
                }
            }
            describe("Unhappy paths") {
                it("should return status Unauthorized if no token is supplied") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeilederinfoSelf)

                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
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

                val graphapiUserResponse = graphapiUserResponse.value[0]

                it("should return OK if request is successful") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeilederinfoNotSelf) {
                            bearerAuth(validTokenVeileder2)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val veilederInfoDTO = response.body<VeilederInfo>()

                        veilederInfoDTO.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT
                        veilederInfoDTO.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                        veilederInfoDTO.etternavn shouldBeEqualTo graphapiUserResponse.surname
                        veilederInfoDTO.epost shouldBeEqualTo graphapiUserResponse.mail
                        veilederInfoDTO.enabled shouldBeEqualTo true
                    }
                }

                it("should return OK for veileder not enabled in graph api") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get("$basePath/${UserConstants.VEILEDER_IDENT_2}") {
                            bearerAuth(validTokenVeileder2)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val veilederInfoDTO = response.body<VeilederInfo>()

                        veilederInfoDTO.ident shouldBeEqualTo UserConstants.VEILEDER_IDENT_2
                        veilederInfoDTO.fornavn shouldBeEqualTo graphapiUserResponse.givenName
                        veilederInfoDTO.etternavn shouldBeEqualTo graphapiUserResponse.surname
                        veilederInfoDTO.epost shouldBeEqualTo graphapiUserResponse.mail
                        veilederInfoDTO.enabled shouldBeEqualTo false
                    }
                }
            }
            describe("Unhappy paths") {
                it("should return status Unauthorized if no token is supplied") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeilederinfoNotSelf)

                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
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

                val axsysResponse = generateAxsysResponse()

                val graphapiUserResponse = graphapiUserResponse.value.first()
                val redisCache = externalMockEnvironment.redisCache
                val cacheKey = "${AxsysClient.AXSYS_CACHE_KEY_PREFIX}${UserConstants.ENHET_NR}"

                it("should return OK if request is successful and veilederlist should be cached") {
                    redisCache.getListObject<AxsysVeileder>(cacheKey) shouldBeEqualTo null
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeiledereEnhetNr) {
                            bearerAuth(validTokenVeileder)
                        }

                        response.status shouldBeEqualTo HttpStatusCode.OK
                        val veilederInfoList = response.body<List<VeilederInfo>>()

                        veilederInfoList.size shouldBeEqualTo axsysResponse.size

                        veilederInfoList.first().ident shouldBeEqualTo axsysResponse.first().appIdent
                        veilederInfoList.first().fornavn shouldBeEqualTo graphapiUserResponse.givenName
                        veilederInfoList.first().etternavn shouldBeEqualTo graphapiUserResponse.surname
                        veilederInfoList.first().enabled shouldBeEqualTo true

                        veilederInfoList.last().ident shouldBeEqualTo axsysResponse.last().appIdent
                        veilederInfoList.last().fornavn shouldBeEqualTo ""
                        veilederInfoList.last().etternavn shouldBeEqualTo ""
                        veilederInfoList.last().enabled.shouldBeNull()
                        redisCache.getListObject<AxsysVeileder>(cacheKey) shouldNotBeEqualTo null
                    }
                }
            }
            describe("Unhappy paths") {
                it("should return status Unauthorized if no token is supplied") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(urlVeiledereEnhetNr)

                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                    }
                }
            }
        }
    }
})
