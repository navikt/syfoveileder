package no.nav.syfo.veiledernavn

import no.nav.syfo.LocalApplication
import no.nav.syfo.Veileder
import no.nav.syfo.util.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.boot.test.context.SpringBootTest


@RunWith(MockitoJUnitRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [LocalApplication::class])
class VeilederServiceTest {

    @Mock
    lateinit var graphApiConsumer: GraphApiConsumer

    @Mock
    lateinit var axsysConsumer: AxsysConsumer

    @InjectMocks
    private lateinit var veilederService: VeilederService

    private val ENHET_NR: String = "9999"

    @Before
    fun setup() {
        given(axsysConsumer.getAxsysVeiledere(ENHET_NR)).willReturn(TestData.AxsysVeiledere)
        given(graphApiConsumer.getVeiledere(TestData.AxsysVeiledere)).willReturn(TestData.AADVeiledere)
    }

    @Test
    fun getVeiledere() {
        val veiledere: List<Veileder> = veilederService.getVeiledere(ENHET_NR)

        assertThat(veiledere).isEqualTo(listOf(
                Veileder("Z999999", "Dana", "Scully"),
                Veileder("Z888888", "", "")
        ))
    }

}
