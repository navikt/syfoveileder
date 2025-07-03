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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class VeiledereApiTest {
    private val basePath = "/syfoveileder/api/v3/veiledere"

    companion object {
        private lateinit var externalMockEnvironment: ExternalMockEnvironment

        @JvmStatic
        @BeforeAll
        fun setup() {
            externalMockEnvironment = ExternalMockEnvironment()
            externalMockEnvironment.startExternalMocks()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            externalMockEnvironment.stopExternalMocks()
        }
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
    inner class `Get Veilederinfo for self` {
        private val urlVeilederinfoSelf = "$basePath/self"

        private val validTokenVeileder1 = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = UserConstants.VEILEDER_IDENT,
        )

        @Nested
        inner class `Happy path` {

            @Test
            fun `should return OK if request is successful and graphapi response should be cached`() {
                val graphapiUserResponse = graphapiUserResponse.value[0]
                val redisCache = externalMockEnvironment.redisCache
                val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}${UserConstants.VEILEDER_IDENT}"

                assertNull(redisCache.getObject<GraphApiUser>(cacheKey))
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoSelf) {
                        bearerAuth(validTokenVeileder1)
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfo = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT, veilederInfo.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfo.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfo.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfo.epost)
                    assertEquals(true, veilederInfo.enabled)
                    assertNotNull(redisCache.getObject<GraphApiUser>(cacheKey))
                }
            }
        }

        @Nested
        inner class `Unhappy paths` {
            @Test
            fun `should return status Unauthorized if no token is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoSelf)

                    assertEquals(HttpStatusCode.Unauthorized, response.status)
                }
            }
        }
    }

    @Nested
    inner class `Get Veilederinfo for veileder ident` {
        private val urlVeilederinfoNotSelf = "$basePath/${UserConstants.VEILEDER_IDENT}"

        private val validTokenVeileder2 = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = UserConstants.VEILEDER_IDENT_2,
        )

        @Nested
        inner class `Happy path` {

            val graphapiUserResponse = no.nav.syfo.testhelper.mock.graphapiUserResponse.value[0]

            @Test
            fun `should return OK if request is successful`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoNotSelf) {
                        bearerAuth(validTokenVeileder2)
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfoDTO = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT, veilederInfoDTO.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfoDTO.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfoDTO.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfoDTO.epost)
                    assertEquals(true, veilederInfoDTO.enabled)
                }
            }

            @Test
            fun `should return OK for veileder not enabled in graph api`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get("$basePath/${UserConstants.VEILEDER_IDENT_2}") {
                        bearerAuth(validTokenVeileder2)
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfoDTO = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT_2, veilederInfoDTO.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfoDTO.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfoDTO.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfoDTO.epost)
                    assertEquals(false, veilederInfoDTO.enabled)
                }
            }
        }

        @Nested
        inner class `Unhappy paths` {
            @Test
            fun `should return status Unauthorized if no token is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoNotSelf)

                    assertEquals(HttpStatusCode.Unauthorized, response.status)
                }
            }
        }
    }

    @Nested
    inner class `Get list of Veiledere for enhetNr` {
        private val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"

        private val validTokenVeileder = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = UserConstants.VEILEDER_IDENT,
        )

        @Nested
        inner class `Happy path` {
            val axsysResponse = generateAxsysResponse()
            val graphapiUserResponse = no.nav.syfo.testhelper.mock.graphapiUserResponse.value.first()
            val redisCache = externalMockEnvironment.redisCache
            val cacheKey = "${AxsysClient.AXSYS_CACHE_KEY_PREFIX}${UserConstants.ENHET_NR}"

            @Test
            fun `should return OK if request is successful and veilederlist should be cached`() {
                assertNull(redisCache.getListObject<AxsysVeileder>(cacheKey))
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeiledereEnhetNr) {
                        bearerAuth(validTokenVeileder)
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfoList = response.body<List<VeilederInfo>>()

                    assertEquals(axsysResponse.size, veilederInfoList.size)

                    assertEquals(axsysResponse.first().appIdent, veilederInfoList.first().ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfoList.first().fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfoList.first().etternavn)
                    assertEquals(true, veilederInfoList.first().enabled)

                    assertEquals(axsysResponse.last().appIdent, veilederInfoList.last().ident)
                    assertEquals("", veilederInfoList.last().fornavn)
                    assertEquals("", veilederInfoList.last().etternavn)
                    assertNull(veilederInfoList.last().enabled)
                    assertNotNull(redisCache.getListObject<AxsysVeileder>(cacheKey))
                }
            }
        }

        @Nested
        inner class `Unhappy paths` {
            @Test
            fun `should return status Unauthorized if no token is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeiledereEnhetNr)

                    assertEquals(HttpStatusCode.Unauthorized, response.status)
                }
            }
        }
    }
}
