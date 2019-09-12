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
        val navneListe = graphApiConsumer.getVeiledere(enhetNr)
        return axsysConsumer.getAxsysVeiledere(enhetNr)
                .map { axsysVeileder -> navneListe.find { it.ident == axsysVeileder.appIdent } ?: axsysVeileder.toVeileder() }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
