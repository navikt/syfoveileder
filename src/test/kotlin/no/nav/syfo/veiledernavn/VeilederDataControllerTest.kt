package no.nav.syfo.veiledernavn

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.AADToken
import no.nav.syfo.AADVeileder
import no.nav.syfo.GetUsersResponse
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.OIDCIssuer
import no.nav.syfo.util.TestUtils
import no.nav.syfo.util.TestUtils.loggInnSomVeileder

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import javax.inject.Inject


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@AutoConfigureMockMvc
@DirtiesContext
class VeilederDataComponentTest {


    @MockBean
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var aadTokenService: AADTokenService

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private val ident = "Z999999"
    private val enhet = "0123"
    private val enhetNavn = "NAV X-files"

    val aadVeilederListe: List<AADVeileder> = listOf(
            AADVeileder(givenName="Dana", surname = "Scully",
                    onPremisesSamAccountName = "Z999999", streetAddress = "0123",
                    city = "Nav X-Files"))

    val veilederListe: String = "[{\"ident\":\"Z999999\",\"fornavn\":\"Dana\"," +
            "\"etternavn\":\"Scully\",\"enhetNr\":\"0123\",\"enhetNavn\":\"Nav X-Files\"}]"

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, ident)
    }

    @After
    fun cleanUp() {
        TestUtils.loggUt(oidcRequestContextHolder)
    }


    @Test
    fun hentVeilederNavn() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken
        mockAADToken()
        mockUsersReponse()

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet/enhetNavn/$enhetNavn")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        assertThat(respons).isEqualTo(veilederListe)
    }

    private fun mockAADToken() {
        val token = AADToken("token", "refreshtoken",
                LocalDateTime.parse("2019-01-01T10:00:00"))
        BDDMockito.given(aadTokenService.getAADToken()).willReturn(token)
        BDDMockito.given(aadTokenService.renewTokenIfExpired(token)).willReturn(AADToken("token", "refreshtoken",
                LocalDateTime.parse("2019-01-01T10:00:00")))

    }

    private fun mockUsersReponse() {
        val getUsersResponse = GetUsersResponse(value = aadVeilederListe)
        BDDMockito.`when`(restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(GetUsersResponse::class.java)
        )).thenReturn(ResponseEntity(getUsersResponse, HttpStatus.OK))
    }

}
