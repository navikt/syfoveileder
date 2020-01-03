package no.nav.syfo.veiledernavn

import no.nav.syfo.Veileder
import no.nav.syfo.toVeileder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VeilederService(
        private val graphApiConsumer: GraphApiConsumer,
        private val axsysConsumer: AxsysConsumer
) {

    fun getVeiledere(enhetNr: String): List<Veileder> {
        val axsysVeiledere = axsysConsumer.getAxsysVeiledere(enhetNr);
        val graphApiVeiledere = graphApiConsumer.getVeiledere(axsysVeiledere)

        return axsysVeiledere.map { axsysVeileder ->
            graphApiVeiledere.find { it.ident == axsysVeileder.appIdent } ?: axsysVeileder.toVeileder()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
