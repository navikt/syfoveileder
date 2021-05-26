package no.nav.syfo.veilederinfo

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.util.*
import no.nav.syfo.util.TestUtils.loggInnSomVeileder
import no.nav.syfo.util.TestUtils.loggUt
import no.nav.syfo.veilederinfo.VeilederInfoController.Companion.API_VEILEDER_BASE_PATH
import no.nav.syfo.veilederinfo.VeilederInfoController.Companion.API_VEILEDER_SELF_PATH
import no.nav.syfo.veiledernavn.AADTokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [LocalApplication::class],
)
@AutoConfigureMockMvc
@DirtiesContext
class VeilederInfoControllerTest {

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var aadTokenService: AADTokenConsumer

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private lateinit var mockRestServiceServer: MockRestServiceServer

    private val graphReponse = graphApiGetUserResponse.copy()

    private val veilederInfo = graphReponse.value.first()
        .toVeilederInfo(VEILEDER_IDENT)
        .toVeilederDTO()

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, VEILEDER_IDENT)
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @After
    fun cleanUp() {
        mockRestServiceServer.verify()
        loggUt(oidcRequestContextHolder)
    }

    @Test
    fun `Get VeilederInfo for Self`() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADTokenConsumer(aadTokenService)
        mockGetUsersResponse(mockRestServiceServer)

        val url = "$API_VEILEDER_BASE_PATH$API_VEILEDER_SELF_PATH"
        val response = mockMvc.perform(MockMvcRequestBuilders.get(url)
            .header(NAV_CALL_ID_HEADER, createCallId())
            .header(AUTHORIZATION, "Bearer $idToken"))
            .andReturn().response

        val expectedResponse = objectMapper.writeValueAsString(veilederInfo)

        assertThat(response.contentAsString).isEqualTo(expectedResponse)
    }

    @Test
    fun `Get VeilederInfo for Self wihout info in Graph throws exception`() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADTokenConsumer(aadTokenService)
        mockGetUsersResponse(
            mockRestServiceServer = mockRestServiceServer,
            response = null,
        )

        val url = "$API_VEILEDER_BASE_PATH$API_VEILEDER_SELF_PATH"
        val response = mockMvc.perform(MockMvcRequestBuilders.get(url)
            .header(NAV_CALL_ID_HEADER, createCallId())
            .header(AUTHORIZATION, "Bearer $idToken"))
            .andReturn().response

        assertThat(response.status).isEqualTo(500)
    }
}
