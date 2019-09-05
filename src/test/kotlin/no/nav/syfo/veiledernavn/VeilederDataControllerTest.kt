package no.nav.syfo.veiledernavn

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.AADToken
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.*
import no.nav.syfo.util.TestData.errorResponseBodyGraphApi
import no.nav.syfo.util.TestData.userListEmptyValueResponseBody
import no.nav.syfo.util.TestData.userListResponseBody
import no.nav.syfo.util.TestUtils.loggInnSomVeileder
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount.manyTimes
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import javax.inject.Inject
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@AutoConfigureMockMvc
@DirtiesContext
class VeilederDataComponentTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var aadTokenService: AADTokenConsumer

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private lateinit var mockRestServiceServer: MockRestServiceServer

    private val ident = "Z999999"
    private val enhet = "0123"
    private val token = AADToken(
            "token",
            "refreshtoken",
            LocalDateTime.parse("2019-01-01T10:00:00")
    )

    private val veilederListe: String = "[{\"ident\":\"Z999999\",\"fornavn\":\"Dana\"," +
            "\"etternavn\":\"Scully\",\"enhetNr\":\"0123\",\"enhetNavn\":\"NAV X-FILES\"}]"

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, ident)
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        MockUtils.mockNorg2Response(mockRestServiceServer)
    }

    @After
    fun cleanUp() {
        mockRestServiceServer.verify()
        TestUtils.loggUt(oidcRequestContextHolder)
    }

    @Test
    fun hentVeilederNavn() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADToken()
        mockGetUsersResponse()

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        assertThat(respons).isEqualTo(veilederListe)
    }

    @Test
    fun ingenVeilederNavn() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADToken()
        mockEmptyGetUsersResponse()

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response

        assertThat(respons.contentAsString).isEqualTo("[]")
        assertThat(respons.status).isEqualTo(200)
    }

    @Test
    fun feilHosAvhengighet() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADToken()
        mockGetUsersResponse500()

        mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet")
                .header("Authorization", "Bearer $idToken"))
                .andExpect(status().isFailedDependency())
                .andReturn().response.contentAsString
    }

    private fun mockGetUsersResponse500() {
        mockRestServiceServer.expect(manyTimes(), anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
                .andRespond(withServerError()
                        .body(errorResponseBodyGraphApi)
                        .contentType(MediaType.APPLICATION_JSON))
    }

    private fun mockEmptyGetUsersResponse() {
        mockRestServiceServer.expect(manyTimes(), anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
                .andRespond(withSuccess()
                        .body(userListEmptyValueResponseBody)
                        .contentType(MediaType.APPLICATION_JSON))
    }

    private fun mockAADToken() {
        BDDMockito.given(aadTokenService.getAADToken()).willReturn(token)
        BDDMockito.given(aadTokenService.renewTokenIfExpired(token)).willReturn(AADToken("token", "refreshtoken",
                LocalDateTime.parse("2019-01-01T10:00:00")))

    }

    private fun mockGetUsersResponse() {
        mockRestServiceServer.expect(manyTimes(), anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
                .andRespond(withSuccess()
                                .body(userListResponseBody)
                                .contentType(MediaType.APPLICATION_JSON))
    }


}
