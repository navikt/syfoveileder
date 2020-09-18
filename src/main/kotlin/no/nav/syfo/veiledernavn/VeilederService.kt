package no.nav.syfo.veiledernavn

import no.nav.syfo.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VeilederService(
        private val graphApiConsumer: GraphApiConsumer,
        private val axsysConsumer: AxsysConsumer
) {

    fun getVeiledere(enhetNr: String): List<Veileder> {
        val axsysVeiledere = axsysConsumer.getAxsysVeiledere(enhetNr)
        val graphApiVeiledere = graphApiConsumer.getVeiledere(axsysVeiledere)

        return axsysVeiledere.map { axsysVeileder ->
            graphApiVeiledere.find { it.ident == axsysVeileder.appIdent } ?: noGraphApiVeileder(axsysVeileder)
        }
    }

    fun noGraphApiVeileder(axsysVeileder: AxsysVeileder): Veileder {
        LOG.warn("Fant ikke navn for veileder i graphApi! Feillederident: ${axsysVeileder.appIdent}")
        return axsysVeileder.toVeileder()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
