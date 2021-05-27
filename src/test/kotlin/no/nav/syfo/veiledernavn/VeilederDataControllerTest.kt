package no.nav.syfo.veiledernavn

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.AADToken
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.OIDCIssuer
import no.nav.syfo.util.TestData.brukereResponseBody
import no.nav.syfo.util.TestData.errorResponseBodyGraphApi
import no.nav.syfo.util.TestData.userListResponseBodyGraphApi
import no.nav.syfo.util.TestUtils
import no.nav.syfo.util.TestUtils.loggInnSomVeileder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.ExpectedCount.manyTimes
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [LocalApplication::class],
)
@AutoConfigureMockMvc
@DirtiesContext
class VeilederDataControllerTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var aadTokenService: AADTokenConsumer

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var tokenValidationContextHolder: TokenValidationContextHolder

    private lateinit var mockRestServiceServer: MockRestServiceServer

    private val ident = "Z999999"
    private val enhet = "0123"
    private val token = AADToken(
        "token",
        "refreshtoken",
        LocalDateTime.parse("2019-01-01T10:00:00")
    )

    private val veilederListe: String = "[" +
        "{\"ident\":\"Z999999\",\"fornavn\":\"Dana\",\"etternavn\":\"Scully\"}," +
        "{\"ident\":\"Z666666\",\"fornavn\":\"\",\"etternavn\":\"\"}" +
        "]"

    @BeforeEach
    fun setup() {
        loggInnSomVeileder(tokenValidationContextHolder, ident)
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @AfterEach
    fun cleanUp() {
        mockRestServiceServer.verify()
        TestUtils.loggUt(tokenValidationContextHolder)
    }

    @Test
    fun getVeilederNames() {
        val idToken = tokenValidationContextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.AZURE).tokenAsString
        mockAADToken()
        mockAxsysVeiledere()
        mockGetUsersResponse()

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet")
            .header("Authorization", "Bearer $idToken"))
            .andReturn().response.contentAsString

        assertThat(respons).isEqualTo(veilederListe)
    }

    @Test
    fun dependencyError() {
        val idToken = tokenValidationContextHolder.tokenValidationContext.getJwtToken(OIDCIssuer.AZURE).tokenAsString
        mockAADToken()
        mockAxsysVeiledere()
        mockGetUsersResponse500()

        mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet")
            .header("Authorization", "Bearer $idToken"))
            .andExpect(status().isFailedDependency)
            .andReturn().response.contentAsString
    }

    private fun mockGetUsersResponse500() {
        mockRestServiceServer.expect(manyTimes(), anything())
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
            .andRespond(withServerError()
                .body(errorResponseBodyGraphApi)
                .contentType(MediaType.APPLICATION_JSON))
    }

    private fun mockAADToken() {
        BDDMockito.given(aadTokenService.getAADToken()).willReturn(token)
        BDDMockito.given(aadTokenService.renewTokenIfExpired(token)).willReturn(AADToken("token", "refreshtoken",
            LocalDateTime.parse("2019-01-01T10:00:00")))

    }

    private fun mockGetUsersResponse() {
        mockRestServiceServer.expect(manyTimes(), anything())
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
            .andRespond(withSuccess()
                .body(userListResponseBodyGraphApi)
                .contentType(MediaType.APPLICATION_JSON))
    }

    private fun mockAxsysVeiledere() {
        mockRestServiceServer.expect(manyTimes(), anything())
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Nav-Call-Id", "default"))
            .andExpect(header("Nav-Consumer-Id", "srvsyfoveileder"))
            .andRespond(withSuccess()
                .body(brukereResponseBody)
                .contentType(MediaType.APPLICATION_JSON))
    }

}
