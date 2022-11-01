package no.nav.syfo.veiledernavn

import no.nav.syfo.client.axsys.*
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.toVeilederInfo
import no.nav.syfo.veileder.*
import org.slf4j.LoggerFactory

class VeilederService(
    private val axsysClient: AxsysClient,
    private val graphApiClient: GraphApiClient,
) {
    suspend fun veilederInfo(
        callId: String,
        token: String,
        veilederIdent: String,
    ): VeilederInfo {
        val graphApiUser = graphApiClient.veileder(
            callId = callId,
            token = token,
            veilederIdent = veilederIdent,
        ).value.firstOrNull()
        return graphApiUser?.toVeilederInfo(veilederIdent)
            ?: throw RuntimeException("User was not found in Microsoft Graph for ident $veilederIdent")
    }

    suspend fun getVeiledere(
        callId: String,
        enhetNr: String,
        token: String,
    ): List<Veileder> {
        val axsysVeilederList = axsysClient.veilederList(
            callId = callId,
            enhetNr = enhetNr,
            token = token,
        )
        val graphApiVeiledere = graphApiClient.veilederList(
            axsysVeilederlist = axsysVeilederList,
            callId = callId,
            token = token,
        )

        val missingInGraphAPI = mutableListOf<String>()
        val returnList = axsysVeilederList.map { axsysVeileder ->
            graphApiVeiledere.find { it.ident == axsysVeileder.appIdent } ?: noGraphApiVeileder(
                axsysVeileder,
                missingInGraphAPI
            )
        }
        if (missingInGraphAPI.isNotEmpty()) {
            log.warn("Fant ikke navn for ${missingInGraphAPI.size} av ${axsysVeilederList.size} veiledere i graphApi! Feilende identer: ${missingInGraphAPI.joinToString()}")
        }
        return returnList
    }

    fun noGraphApiVeileder(
        axsysVeileder: AxsysVeileder,
        missingInGraphAPI: MutableList<String>,
    ): Veileder {
        missingInGraphAPI.add(axsysVeileder.appIdent)
        return axsysVeileder.toVeileder()
    }

    companion object {
        private val log = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
