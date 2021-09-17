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

        val missingInGraphAPI = mutableListOf<String>()
        val returnList = axsysVeiledere.map { axsysVeileder ->
            graphApiVeiledere.find { it.ident == axsysVeileder.appIdent } ?: noGraphApiVeileder(axsysVeileder, missingInGraphAPI)
        }
        if (missingInGraphAPI.isNotEmpty()) {
            LOG.warn("Fant ikke navn for ${missingInGraphAPI.size} av ${axsysVeiledere.size} veiledere i graphApi! Feilende identer: ${missingInGraphAPI.joinToString()}")
        }
        return returnList
    }

    fun noGraphApiVeileder(axsysVeileder: AxsysVeileder, missingInGraphAPI: MutableList<String>): Veileder {
        metric.countEvent("veileder_name_missing")
        missingInGraphAPI.add(axsysVeileder.appIdent)
        return axsysVeileder.toVeileder()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
