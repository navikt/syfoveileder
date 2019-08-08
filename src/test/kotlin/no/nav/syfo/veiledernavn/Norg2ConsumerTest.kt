package no.nav.syfo.veiledernavn

import no.nav.syfo.LocalApplication
import no.nav.syfo.util.MockUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.*
import javax.inject.Inject
import javax.ws.rs.BadRequestException
import javax.ws.rs.InternalServerErrorException

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
@DirtiesContext
class Norg2ConsumerTest {

    @Inject
    lateinit var restTemplate: RestTemplate

    @Inject
    lateinit var norg2Consumer: Norg2Consumer

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
    fun hentEnhetNavn() {
        MockUtils.mockNorg2Response(mockRestServiceServer)
        val enhetNavn = norg2Consumer.hentEnhetNavn("0123")

        assertThat(enhetNavn).isEqualTo("NAV X-Files")
    }

    @Test(expected = BadRequestException::class)
    fun enhetsNummerFinnesIkke() {
        MockUtils.mockNorg2EnhetsNummerFinnesIkke(mockRestServiceServer)
        norg2Consumer.hentEnhetNavn("0000")
    }
}
