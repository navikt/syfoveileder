package no.nav.syfo.veileder.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.util.getBearerHeader
import no.nav.syfo.util.getCallId
import no.nav.syfo.veileder.*
import no.nav.syfo.veiledernavn.VeilederService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

const val basePath = "/syfoveileder/api/v3"

fun Route.registrerVeiledereApi(
    veilederService: VeilederService,
) {
    route(basePath) {
        get("/veiledere") {
            val callId = getCallId()
            val enhetNr = call.request.queryParameters["enhetNr"]
                ?: throw IllegalArgumentException("No EnhetNr found in query param")
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val veilederList = veilederService.getVeiledere(
                    callId = callId,
                    enhetNr = enhetNr,
                    token = token,
                )
                call.respond<List<VeilederInfo>>(veilederList)
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve VeilederList for enhetNr:"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }

        get("/veiledere/self") {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val veilederIdent: String = getNAVIdentFromToken(token = token)

                val veilederinfo = veilederService.veilederInfo(
                    callId = callId,
                    token = token,
                    veilederIdent = veilederIdent,
                )
                veilederinfo?.let {
                    call.respond<VeilederInfo>(it)
                } ?: call.respond(HttpStatusCode.NotFound, "User was not found in Microsoft Graph for ident $veilederIdent")
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve Veilederinfo for self"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }

        get("/veiledere/{navIdent}") {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val veilederIdent = call.parameters["navIdent"]
                    ?: throw IllegalArgumentException("No VeilederIdent found in path param")

                val veilederinfo = veilederService.veilederInfo(
                    callId = callId,
                    veilederIdent = veilederIdent,
                    token = token,
                )
                veilederinfo?.let {
                    call.respond<VeilederInfo>(it)
                } ?: call.respond(HttpStatusCode.NotFound, "User was not found in Microsoft Graph for ident $veilederIdent")
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve Veilederinfo for NavIdent"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }
    }
}
