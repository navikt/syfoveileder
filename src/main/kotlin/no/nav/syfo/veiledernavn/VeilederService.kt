package no.nav.syfo.veiledernavn

import no.nav.syfo.*
import no.nav.syfo.metric.Metric
import no.nav.syfo.veilederinfo.VeilederInfo
import no.nav.syfo.veilederinfo.toVeilederInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VeilederService(
        private val graphApiConsumer: GraphApiConsumer,
        private val metric: Metric,
        private val axsysConsumer: AxsysConsumer
) {
    fun veilederInfo(
        callId: String,
        veilederIdent: String
    ): VeilederInfo {
        val graphApiUser = graphApiConsumer.veileder(
            callId = callId,
            veilederIdent = veilederIdent,
        ).value.firstOrNull()
        graphApiUser?.let {
            return it.toVeilederInfo(veilederIdent)
        } ?: throw RuntimeException("User was not found in Microsoft Graph for ident$veilederIdent")
    }

    fun getVeiledere(enhetNr: String): List<Veileder> {
        val axsysVeiledere = axsysConsumer.getAxsysVeiledere(enhetNr)
        val graphApiVeiledere = graphApiConsumer.getVeiledere(axsysVeiledere)

        return axsysVeiledere.map { axsysVeileder ->
            graphApiVeiledere.find { it.ident == axsysVeileder.appIdent } ?: noGraphApiVeileder(axsysVeileder)
        }
    }

    fun noGraphApiVeileder(axsysVeileder: AxsysVeileder): Veileder {
        LOG.warn("Fant ikke navn for veileder i graphApi! Feillederident: ${axsysVeileder.appIdent}")
        metric.countEvent("veileder_name_missing")
        return axsysVeileder.toVeileder()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
