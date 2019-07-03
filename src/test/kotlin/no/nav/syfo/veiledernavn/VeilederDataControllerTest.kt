package no.nav.syfo.veiledernavn

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.*
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
import org.springframework.http.*
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount.manyTimes
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
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

    @Inject
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var aadTokenService: AADTokenService

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private lateinit var mockRestServiceServer: MockRestServiceServer

    private val ident = "Z999999"
    private val enhet = "0123"
    private val enhetNavn = "NAV X-files"
    private val token = AADToken(
            "token",
            "refreshtoken",
            LocalDateTime.parse("2019-01-01T10:00:00")
    )
    private val userListResponseBody = "{\n" +
            "    \"@odata.context\": \"https://graph.microsoft.com/v1.0/\$metadata#users(onPremisesSamAccountName,givenName,surname,streetAddress,city)\",\n" +
            "    \"@odata.nextLink\": \"https://graph.microsoft.com/v1.0/users/?\$filter=city+eq+'NAV%20X-files'&\$select=onPremisesSamAccountName%2cgivenName%2csurname%2cstreetAddress%2ccity&\$skiptoken=X'44537074090001000000000000000014000000DDE2A3E7B5A9244DB391C7B5E55D1DF201000000000000000000000000000017312E322E3834302E3131333535362E312E342E3233333102000000000001C7D60D18A735D441B7703E043EA6192D'\",\n" +
            "    \"value\": [\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z777777\",\n" +
            "            \"givenName\": \"A.D \",\n" +
            "            \"surname\": \"Skinner\",\n" +
            "            \"streetAddress\": \"2990\",\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z888888\",\n" +
            "            \"givenName\": \"Fox\",\n" +
            "            \"surname\": \"Mulder\",\n" +
            "            \"streetAddress\": null,\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z999999\",\n" +
            "            \"givenName\": \"Dana\",\n" +
            "            \"surname\": \"Scully\",\n" +
            "            \"streetAddress\": \"0123\",\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        }\n" +
            "    ]\n" +
            "}"

    val veilederListe: String = "[{\"ident\":\"Z999999\",\"fornavn\":\"Dana\"," +
            "\"etternavn\":\"Scully\",\"enhetNr\":\"0123\",\"enhetNavn\":\"NAV X-FILES\"}]"

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, ident)
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
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

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veiledere/enhet/$enhet/enhetNavn/$enhetNavn")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        assertThat(respons).isEqualTo(veilederListe)
    }

    private fun mockAADToken() {
        BDDMockito.given(aadTokenService.getAADToken()).willReturn(token)
        BDDMockito.given(aadTokenService.renewTokenIfExpired(token)).willReturn(AADToken("token", "refreshtoken",
                LocalDateTime.parse("2019-01-01T10:00:00")))

    }


    private fun mockGetUsersResponse() {
        val uri: String =  "https://graph.microsoft.com/v1.0/users/?${'$'}filter=city%20eq%20'NAV%20X-files'&${'$'}select=onPremisesSamAccountName,givenName,surname,streetAddress,city"

        mockRestServiceServer.expect(manyTimes(), requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer ${token.accessToken}"))
                .andRespond(withSuccess()
                                .body(userListResponseBody)
                                .contentType(MediaType.APPLICATION_JSON))
    }


}
