package no.nav.syfo.veiledernavn

import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.client.axsys.toVeilederInfo
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.veileder.Veileder.Companion.toVeilederInfo
import no.nav.syfo.client.graphapi.toVeilederInfo
import no.nav.syfo.veileder.VeilederInfo
import org.slf4j.LoggerFactory

class VeilederService(
    private val axsysClient: AxsysClient,
    private val graphApiClient: GraphApiClient,
) {
    suspend fun veilederInfo(
        callId: String,
        token: String,
        veilederIdent: String,
    ): VeilederInfo? {
        val graphApiUser = graphApiClient.veileder(
            callId = callId,
            token = token,
            veilederIdent = veilederIdent,
        )
        if (graphApiUser?.accountEnabled == false) {
            log.warn("Veileder with ident $veilederIdent is not enabled in Microsoft Graph")
        }
        return graphApiUser?.toVeilederInfo(veilederIdent)
    }

    suspend fun veilederInfoMedSystemToken(
        callId: String,
        token: String,
        veilederIdent: String,
    ): VeilederInfo? = graphApiClient.veilederMedSystemToken(
        callId = callId,
        token = token,
        veilederIdent = veilederIdent,
    )?.toVeilederInfo(veilederIdent)

    suspend fun getVeiledere(
        callId: String,
        enhetNr: String,
        token: String,
    ): List<VeilederInfo> {
        val axsysVeilederList = axsysClient.veilederList(
            callId = callId,
            enhetNr = enhetNr,
            token = token,
        )
        val graphApiUsers = graphApiClient.veilederList(
            enhetNr = enhetNr,
            axsysVeilederlist = axsysVeilederList,
            callId = callId,
            token = token,
        )
        val usersNotEnabled = graphApiUsers.filter { !it.accountEnabled }.map { it.onPremisesSamAccountName }
        if (usersNotEnabled.isNotEmpty()) {
            log.warn("Fant ${usersNotEnabled.size} veiledere i Microsoft Graph som er disabled. Identer: ${usersNotEnabled.joinToString()}")
        }
        val veiledere = graphApiUsers.map { it.toVeilederInfo(it.onPremisesSamAccountName) }

        val missingInGraphAPI = mutableListOf<String>()
        val returnList: List<VeilederInfo> = axsysVeilederList.map { axsysVeileder ->
            veiledere.find { it.ident == axsysVeileder.appIdent } ?: noGraphApiVeileder(
                axsysVeileder,
                missingInGraphAPI,
            )
        }
        if (missingInGraphAPI.isNotEmpty()) {
            log.warn("Fant ikke navn for ${missingInGraphAPI.size} av ${axsysVeilederList.size} veiledere i graphApi! Feilende identer: ${missingInGraphAPI.joinToString()}")
        }

        val veilederInfo = graphApiClient.getVeiledereByEnhetNr(
            token = token,
            enhetNr = enhetNr,
        ).map { it.toVeilederInfo() }

//        return returnList
        return veilederInfo
    }

    private fun noGraphApiVeileder(
        axsysVeileder: AxsysVeileder,
        missingInGraphAPI: MutableList<String>,
    ): VeilederInfo {
        missingInGraphAPI.add(axsysVeileder.appIdent)
        return axsysVeileder.toVeilederInfo()
    }

    companion object {
        private val log = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
