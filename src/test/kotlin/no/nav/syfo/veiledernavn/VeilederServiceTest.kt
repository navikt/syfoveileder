package no.nav.syfo.veiledernavn

import no.nav.syfo.LocalApplication
import no.nav.syfo.Veileder
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [LocalApplication::class],
)
class VeilederServiceTest {

    @Mock
    lateinit var graphApiConsumer: GraphApiConsumer

    @Mock
    lateinit var axsysConsumer: AxsysConsumer

    @Mock
    lateinit var metric: Metric

    @InjectMocks
    private lateinit var veilederService: VeilederService

    private val ENHET_NR: String = "9999"

    @BeforeEach
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
