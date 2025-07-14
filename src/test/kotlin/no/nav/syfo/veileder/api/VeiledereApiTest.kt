package no.nav.syfo.veileder.api

import com.microsoft.graph.models.Group
import com.microsoft.graph.models.User
import com.microsoft.graph.models.odataerrors.MainError
import com.microsoft.graph.models.odataerrors.ODataError
import com.microsoft.graph.serviceclient.GraphServiceClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.GraphApiService
import no.nav.syfo.client.graphapi.GraphApiServiceImpl
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

    private fun ApplicationTestBuilder.setupApiAndClient(graphApiService: GraphApiService = externalMockEnvironment.mockGraphApiService): HttpClient {
        application {
            testApiModule(
                externalMockEnvironment = externalMockEnvironment,
                graphApiService = graphApiService,
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
                val redisCache = externalMockEnvironment.redisCache
                val cacheKey = "${GraphApiClient.GRAPH_API_CACHE_VEILEDER_PREFIX}${UserConstants.VEILEDER_IDENT}"

                assertNull(redisCache.getObject<GraphApiUser>(cacheKey))
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
                    assertNotNull(redisCache.getObject<GraphApiUser>(cacheKey))
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
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                return emptyList()
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val veilederInfo = response.body<List<VeilederInfo>>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(veilederInfo.isEmpty())

            val cachedGrupper = redisCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertTrue(cachedGrupper.isEmpty())
        }
    }

    @Test
    fun `Veileder har grupper, men tilhorer ikke oppgitt enhet`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                return listOf(group(groupId = groupId, enhetNr = "0456"))
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        assertNull(redisCache.getObject<List<Veileder>>(veilederCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val veilederInfo = response.body<List<VeilederInfo>>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, veilederInfo.size)

            val cachedGrupper = redisCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(redisCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }

    @Test
    fun `Veileder har grupper og tilhorer oppgitt enhet`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                return listOf(group(groupId = groupId))
            }

            override fun getMembersByGroupIdRequest(
                graphServiceClient: GraphServiceClient,
                groupId: String
            ): List<User> {
                return listOf(user())
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        assertNull(redisCache.getObject<List<Veileder>>(veilederCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

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

            val cachedGrupper = redisCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)

            val cachedVeiledere = redisCache.getObject<List<Veileder>>(veilederCacheKey)!!
            assertEquals(1, cachedVeiledere.size)
        }
    }

    @Test
    fun `Kall pa grupper for veileder feilet med ODataError (ApiException)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                throw ODataError().apply {
                    error =
                        MainError().apply { this.code = "400" }.apply { this.message = "Error when calling Graph API" }
                }
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Error when calling Graph API", errorMessage)
            assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        }
    }

    @Test
    fun `Kall pa grupper for veileder feilet med IllegalAccessException (Exception)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                throw IllegalAccessException("Some access error")
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Some access error", errorMessage)
            assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        }
    }

    @Test
    fun `Henting av veiledere pa gruppeId feilet med ODataError (ApiException)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                return listOf(group(groupId = groupId))
            }

            override fun getMembersByGroupIdRequest(
                graphServiceClient: GraphServiceClient,
                groupId: String
            ): List<User> {
                throw ODataError().apply {
                    error =
                        MainError().apply { this.code = "400" }.apply { this.message = "Error when calling Graph API" }
                }
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Error when calling Graph API", errorMessage)

            val cachedGrupper = redisCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(redisCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }

    @Test
    fun `Henting av veiledere på gruppeId feilet med IllegalAccessException (Exception)`() {
        val urlVeiledereEnhetNr = "$basePath?enhetNr=${UserConstants.ENHET_NR}"
        val veilederIdent = UserConstants.VEILEDER_IDENT
        val redisCache = externalMockEnvironment.redisCache
        val gruppeCacheKey = GraphApiClient.cacheKeyGrupper(veilederIdent)
        val groupId = "UUID"
        val veilederCacheKey = GraphApiClient.cacheKeyVeiledereIEnhet(groupId)

        val service: GraphApiService = object : GraphApiServiceImpl() {
            override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
                return listOf(group(groupId = groupId))
            }

            override fun getMembersByGroupIdRequest(
                graphServiceClient: GraphServiceClient,
                groupId: String
            ): List<User> {
                throw IllegalAccessException("Some access error")
            }
        }

        assertNull(redisCache.getObject<List<Gruppe>>(gruppeCacheKey))
        testApplication {
            val client = setupApiAndClient(service)

            val response = client.get(urlVeiledereEnhetNr) {
                bearerAuth(getValidTokenVeileder(veilederIdent))
            }

            val errorMessage = response.body<String>()
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("Some access error", errorMessage)

            val cachedGrupper = redisCache.getListObject<Gruppe>(gruppeCacheKey)!!
            assertEquals(1, cachedGrupper.size)
            assertNull(redisCache.getObject<List<Veileder>>(veilederCacheKey))
        }
    }
}
