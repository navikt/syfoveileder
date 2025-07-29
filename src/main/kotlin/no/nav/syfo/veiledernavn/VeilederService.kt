package no.nav.syfo.veiledernavn

import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.graphapi.toVeilederInfo
import no.nav.syfo.veileder.VeilederInfo
import org.slf4j.LoggerFactory

class VeilederService(
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
        return graphApiClient.getEnhetByEnhetNrForVeileder(
            token = token,
            enhetNr = enhetNr,
        )?.let { group ->
            graphApiClient.getVeiledereVedEnhetByGroupId(
                callId = callId,
                token = token,
                group = group,
            )
        } ?: run {
            log.warn("User has no groups or there are no veiledere in specified group. CallId=$callId")
            emptyList()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(VeilederService::class.java.name)
    }
}
