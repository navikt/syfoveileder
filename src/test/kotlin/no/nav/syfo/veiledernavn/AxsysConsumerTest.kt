package no.nav.syfo.veiledernavn

import no.nav.syfo.AxsysVeileder
import no.nav.syfo.LocalApplication
import no.nav.syfo.util.MockUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.*
import javax.inject.Inject
import javax.ws.rs.BadRequestException

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@DirtiesContext
class AxsysConsumerTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @Inject
    lateinit var axsysConsumer: AxsysConsumer

    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    fun setup() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @After
    fun cleanUp() {
        mockRestServiceServer.verify()
    }

    @Test
    fun getVeilederIdenter() {
        MockUtils.mockAxsysResponse(mockRestServiceServer)
        val veilederIdenter: List<AxsysVeileder> = axsysConsumer.getAxsysVeiledere("0123")

        assertThat(veilederIdenter[0].appIdent).isEqualTo("Z999999")
    }

    @Test(expected = BadRequestException::class)
    fun enhetsNummerFinnesIkke() {
        MockUtils.mockAxsysEnhetsNummerFinnesIkke(mockRestServiceServer)
        axsysConsumer.getAxsysVeiledere("0999")
    }
}
