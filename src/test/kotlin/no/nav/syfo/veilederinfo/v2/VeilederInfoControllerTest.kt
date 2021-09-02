package no.nav.syfo.veilederinfo.v2

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_2
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.TestUtils.loggInnSomVeilederV2
import no.nav.syfo.util.TestUtils.loggUt
import no.nav.syfo.util.createCallId
import no.nav.syfo.veilederinfo.toVeilederDTO
import no.nav.syfo.veilederinfo.toVeilederInfo
import no.nav.syfo.consumer.azuread.AzureAdV2TokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [LocalApplication::class],
)
@DirtiesContext
class VeilederInfoControllerTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @MockBean
    lateinit var azureAdV2TokenService: AzureAdV2TokenConsumer

    @Inject
    private lateinit var tokenValidationContextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var veilederInfoController: VeilederInfoControllerV2

    private lateinit var mockRestServiceServer: MockRestServiceServer

    private val graphReponse = graphApiGetUserResponse.copy()

    private val veilederInfo = graphReponse.value.first()
        .toVeilederInfo(VEILEDER_IDENT)
        .toVeilederDTO()

    @BeforeEach
    fun setup() {
        loggInnSomVeilederV2(tokenValidationContextHolder, VEILEDER_IDENT)
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @AfterEach
    fun cleanUp() {
        mockRestServiceServer.verify()
        loggUt(tokenValidationContextHolder)
    }

    @Test
    fun `Get VeilederInfo for Self`() {
        mockAADTokenConsumer(azureAdV2TokenService)
        mockGetUsersResponse(mockRestServiceServer)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_CALL_ID_HEADER, createCallId())
        val response = veilederInfoController.getVeilederInfo(
            headers = headers,
        )
        assertThat(response).isEqualTo(veilederInfo)
    }

    @Test
    fun `Get VeilederInfo for Self wihout info in Graph throws exception`() {
        mockAADTokenConsumer(azureAdV2TokenService)
        mockGetUsersResponse(
            mockRestServiceServer = mockRestServiceServer,
            response = null,
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_CALL_ID_HEADER, createCallId())

        Assertions.assertThrows(RuntimeException::class.java) {
            veilederInfoController.getVeilederInfo(
                headers = headers,
            )
        }
    }

    @Test
    fun `Get VeilederInfo for NAVIdent`() {
        loggUt(tokenValidationContextHolder)
        loggInnSomVeilederV2(tokenValidationContextHolder, VEILEDER_IDENT_2)

        mockAADTokenConsumer(azureAdV2TokenService)
        mockGetUsersResponse(mockRestServiceServer)

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_CALL_ID_HEADER, createCallId())
        val response = veilederInfoController.getVeilederInfo(
            navident = VEILEDER_IDENT,
            headers = headers,
        )
        assertThat(response).isEqualTo(veilederInfo)
    }

    @Test
    fun `Get VeilederInfo for NAVIdent wihout info in Graph throws exception`() {
        loggUt(tokenValidationContextHolder)
        loggInnSomVeilederV2(tokenValidationContextHolder, VEILEDER_IDENT_2)

        mockAADTokenConsumer(azureAdV2TokenService)
        mockGetUsersResponse(
            mockRestServiceServer = mockRestServiceServer,
            response = null,
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_CALL_ID_HEADER, createCallId())

        Assertions.assertThrows(RuntimeException::class.java) {
            veilederInfoController.getVeilederInfo(
                navident = "",
                headers = headers,
            )
        }
    }
}
