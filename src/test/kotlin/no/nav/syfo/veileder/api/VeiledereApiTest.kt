package no.nav.syfo.veileder.api

import com.microsoft.graph.models.odataerrors.MainError
import com.microsoft.graph.models.odataerrors.ODataError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.spyk
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.GraphApiUser
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.mock.graphapiUserResponse
import no.nav.syfo.testhelper.mock.group
import no.nav.syfo.testhelper.mock.user
import no.nav.syfo.util.configure
import no.nav.syfo.veileder.Gruppe
import no.nav.syfo.veileder.Veileder
import no.nav.syfo.veileder.VeilederInfo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VeiledereApiTest {
    private val basePath = "/syfoveileder/api/v3/veiledere"

    private val externalMockEnvironment: ExternalMockEnvironment = ExternalMockEnvironment()

    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        graphApiUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.valkeyCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val graphApiClient = GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.valkeyCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    @BeforeEach
    fun setup() {
        externalMockEnvironment.startExternalMocks()
    }

    @AfterEach
    fun tearDown() {
        externalMockEnvironment.stopExternalMocks()
    }

    private fun ApplicationTestBuilder.setupApiAndClient(
        graphApiClient: GraphApiClient? = null
    ): HttpClient {
        application {
            testApiModule(
                externalMockEnvironment = externalMockEnvironment,
                graphApiClientMock = graphApiClient
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
    @DisplayName("Get Veilederinfo for self")
    inner class GetVeilederinfoForSelf {
        private val urlVeilederinfoSelf = "$basePath/self"

        private fun getValidTokenVeileder1() = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = UserConstants.VEILEDER_IDENT,
        )

        @Nested
        @DisplayName("Happy path")
        inner class HappyPath {

            @Test
            fun `should return OK if request is successful and graphapi response should be cached`() {
                val graphapiUserResponse = graphapiUserResponse.value[0]
                val valkeyCache = externalMockEnvironment.valkeyCache
                val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}${UserConstants.VEILEDER_IDENT}"

                assertNull(valkeyCache.getObject<GraphApiUser>(cacheKey))
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoSelf) {
                        bearerAuth(getValidTokenVeileder1())
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfo = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT, veilederInfo.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfo.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfo.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfo.epost)
                    assertTrue(veilederInfo.enabled!!)
                    assertNotNull(valkeyCache.getObject<GraphApiUser>(cacheKey))
                }
            }
        }

        @Nested
        @DisplayName("Unhappy paths")
        inner class UnhappyPaths {
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
    @DisplayName("Get Veilederinfo for veileder ident")
    inner class GetVeilederinfoForVeilederIdent {
        private val urlVeilederinfoNotSelf = "$basePath/${UserConstants.VEILEDER_IDENT}"

        private fun getValidTokenVeileder2() = generateJWT(
            audience = externalMockEnvironment.environment.azureAppClientId,
            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
            navIdent = UserConstants.VEILEDER_IDENT_2,
        )

        @Nested
        @DisplayName("Happy path")
        inner class HappyPath {

            val graphapiUserResponse = no.nav.syfo.testhelper.mock.graphapiUserResponse.value[0]

            @Test
            fun `should return OK if request is successful`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(urlVeilederinfoNotSelf) {
                        bearerAuth(getValidTokenVeileder2())
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfoDTO = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT, veilederInfoDTO.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfoDTO.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfoDTO.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfoDTO.epost)
                    assertTrue(veilederInfoDTO.enabled!!)
                }
            }

            @Test
            fun `should return OK for veileder not enabled in graph api`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get("$basePath/${UserConstants.VEILEDER_IDENT_2}") {
                        bearerAuth(getValidTokenVeileder2())
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val veilederInfoDTO = response.body<VeilederInfo>()

                    assertEquals(UserConstants.VEILEDER_IDENT_2, veilederInfoDTO.ident)
                    assertEquals(graphapiUserResponse.givenName, veilederInfoDTO.fornavn)
                    assertEquals(graphapiUserResponse.surname, veilederInfoDTO.etternavn)
                    assertEquals(graphapiUserResponse.mail, veilederInfoDTO.epost)
                    assertFalse(veilederInfoDTO.enabled!!)
                }
            }
        }

        @Nested
        @DisplayName("Unhappy paths")
        inner class UnhappyPaths {
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

    @Test
    fun `should return status Unauthorized if no token is supplied`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"

        testApplication {
            val client = setupApiAndClient()
            val response = client.get(urlVeiledereEnhetNr)

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    private fun getValidTokenVeileder(ident: String) = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
        navIdent = ident,
    )

    @Test
    fun `Veileder har ingen grupper`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } returns emptyList()

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val veilederInfo = response.body<List<VeilederInfo>>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(veilederInfo.isEmpty())

            val cachedGrupper = valkeyCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertTrue(cachedGrupper.isEmpty())
        }
    }

    @Test
    fun `Veileder har grupper, men tilhorer ikke oppgitt enhet`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } returns listOf(
            group(
                groupId = groupId,
                enhetNr = "0456"
            )
        )

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        assertNull(valkeyCache.getObject<List<Veileder>>(veilederCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val veilederInfo = response.body<List<VeilederInfo>>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, veilederInfo.size)

            val cachedGrupper = valkeyCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(valkeyCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }

    @Test
    fun `Veileder har grupper og tilhorer oppgitt enhet`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } returns listOf(group(groupId = groupId))
        every { graphApiClientStub.getMembersByGroupIdRequest(any(), any()) } returns listOf(user())

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        assertNull(valkeyCache.getObject<List<Veileder>>(veilederCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val veilederInfo = response.body<List<VeilederInfo>>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, veilederInfo.size)

            val veileder = veilederInfo[0]
            assertEquals(UserConstants.VEILEDER_IDENT, veileder.ident)
            assertEquals("Given", veileder.fornavn)
            assertEquals("Surname", veileder.etternavn)
            assertEquals("given.surname@nav.no", veileder.epost)
            assertEquals("00 00 00 00", veileder.telefonnummer)
            assertTrue(veileder.enabled!!)

            val cachedGrupper = valkeyCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)

            val cachedVeiledere = valkeyCache.getObject<List<Veileder>>(veilederCacheKey)!!
            assertEquals(1, cachedVeiledere.size)
        }
    }

    @Test
    fun `Kall pa grupper for veileder feilet med ODataError (ApiException)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } throws ODataError().apply {
            error =
                MainError().apply { this.code = "400" }.apply { this.message = "Error when calling Graph API" }
        }

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Error when calling Graph API", errorMessage)
            assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        }
    }

    @Test
    fun `Kall pa grupper for veileder feilet med IllegalAccessException (Exception)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } throws IllegalAccessException("Some access error")

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Some access error", errorMessage)
            assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        }
    }

    @Test
    fun `Henting av veiledere pa gruppeId feilet med ODataError (ApiException)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } returns listOf(group(groupId = groupId))
        every { graphApiClientStub.getMembersByGroupIdRequest(any(), any()) } throws ODataError().apply {
            error =
                MainError().apply { this.code = "400" }.apply { this.message = "Error when calling Graph API" }
        }

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Error when calling Graph API", errorMessage)

            val cachedGrupper = valkeyCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(valkeyCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }

    @Test
    fun `Henting av veiledere pa gruppeId feilet med IllegalAccessException (Exception)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val valkeyCache = externalMockEnvironment.valkeyCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val graphApiClientStub = spyk(graphApiClient)
        every { graphApiClientStub.getGroupsForVeilederRequest(any()) } returns listOf(group(groupId = groupId))
        every {
            graphApiClientStub.getMembersByGroupIdRequest(
                any(),
                any()
            )
        } throws IllegalAccessException("Some access error")

        assertNull(valkeyCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(graphApiClient = graphApiClientStub)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Some access error", errorMessage)

            val cachedGrupper = valkeyCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(valkeyCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }
}
