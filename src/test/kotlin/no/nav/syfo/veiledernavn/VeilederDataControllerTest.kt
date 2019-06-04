package no.nav.syfo.veiledernavn

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.OIDCIssuer
import no.nav.syfo.util.TestUtils.loggInnSomVeileder

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@AutoConfigureMockMvc
@DirtiesContext
class VeilederDataComponentTest {

    @Inject
    private lateinit var mockMvc: MockMvc

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    private val ident = "Z999999"

    @Before
    fun setup() {
        loggInnSomVeileder(oidcRequestContextHolder, ident)
    }

    @Test
    fun hentVeilederNavn() {
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.AZURE).idToken

        val respons = mockMvc.perform(MockMvcRequestBuilders.get("/api/veileder/$ident")
                .header("Authorization", "Bearer $idToken"))
                .andReturn().response.contentAsString

        assertThat(respons).isEqualTo("{\"ident\":\"Z999999\",\"navn\":\"Katherine\",\"fornavn\":\"Dana\",\"etternavn\":\"Scully\"}")
    }

}
