package no.nav.syfo.veiledernavn

import no.nav.syfo.AxsysVeileder
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.MockUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import javax.inject.Inject
import javax.ws.rs.BadRequestException

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [LocalApplication::class],
)
@DirtiesContext
class AxsysConsumerTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @Inject
    lateinit var axsysConsumer: AxsysConsumer

    private lateinit var mockRestServiceServer: MockRestServiceServer

    @BeforeEach
    fun setup() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @AfterEach
    fun cleanUp() {
        mockRestServiceServer.verify()
    }

    @Test
    fun getVeilederIdenter() {
        MockUtils.mockAxsysResponse(mockRestServiceServer)
        val veilederIdenter: List<AxsysVeileder> = axsysConsumer.getAxsysVeiledere("0123")

        assertThat(veilederIdenter.first().appIdent).isEqualTo("Z999999")
    }

    @Test
    fun enhetsNummerFinnesIkke() {
        MockUtils.mockAxsysEnhetsNummerFinnesIkke(mockRestServiceServer)

        Assertions.assertThrows(BadRequestException::class.java) {
            axsysConsumer.getAxsysVeiledere("0999")
        }
    }
}
