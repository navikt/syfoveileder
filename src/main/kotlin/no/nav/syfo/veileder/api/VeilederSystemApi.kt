package no.nav.syfo.veileder.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.PreAuthorizedApp
import no.nav.syfo.application.api.authentication.getConsumerClientId
import no.nav.syfo.util.getBearerHeader
import no.nav.syfo.util.getCallId
import no.nav.syfo.veileder.VeilederInfo
import no.nav.syfo.veiledernavn.VeilederService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

const val systemApiBasePath = "/syfoveileder/api/system"
private val veilederSystemApiAuthorizedApps = listOf("syfooversiktsrv")

fun Route.registrerVeilederSystemApi(
    veilederService: VeilederService,
    preAuthorizedApps: List<PreAuthorizedApp>,
) {
    route(systemApiBasePath) {
        get("/veiledere/{navIdent}") {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")
                val consumerClientId = getConsumerClientId(token)
                val veilederSystemApiAuthorizedClientIds = preAuthorizedApps
                    .filter { veilederSystemApiAuthorizedApps.contains(it.getAppnavn()) }
                    .map { it.clientId }
                if (!veilederSystemApiAuthorizedClientIds.contains(consumerClientId)) {
                    call.respond(HttpStatusCode.Forbidden, "Consumer with clientId(azp)=$consumerClientId is denied access to system API")
                } else {
                    val veilederIdent = call.parameters["navIdent"]
                        ?: throw IllegalArgumentException("No VeilederIdent found in path param")

                    val veilederinfo = veilederService.veilederInfoMedSystemToken(
                        callId = callId,
                        token = token,
                        veilederIdent = veilederIdent,
                    )
                    veilederinfo?.let {
                        call.respond<VeilederInfo>(it)
                    } ?: call.respond(HttpStatusCode.NotFound, "User was not found in Microsoft Graph for ident $veilederIdent")
                }
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve Veilederinfo for NavIdent"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }
    }
}
