package no.nav.syfo.veileder.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.util.getBearerHeader
import no.nav.syfo.util.getCallId
import no.nav.syfo.veileder.toVeilederinfoDTO
import no.nav.syfo.veiledernavn.VeilederService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

const val apiVeilederinfoBasePath = "/syfoveileder/api/v2/veileder"
const val apiVeilederinfoSelfPath = "/self"
const val apiVeilederinfoNavIdentParam = "navident"

fun Route.registrerVeilederinfoApi(
    veilederService: VeilederService,
) {
    route(apiVeilederinfoBasePath) {
        get(apiVeilederinfoSelfPath) {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val veilederIdent: String = getNAVIdentFromToken(token = token)

                val veilederinfo = veilederService.veilederInfo(
                    callId = callId,
                    token = token,
                    veilederIdent = veilederIdent,
                ).toVeilederinfoDTO()
                call.respond(veilederinfo)
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve Veilederinfo for self"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }

        get("/{$apiVeilederinfoNavIdentParam}") {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val veilederIdent = call.parameters[apiVeilederinfoNavIdentParam]
                    ?: throw IllegalArgumentException("No VeilederIdent found in path param")

                val veilederinfo = veilederService.veilederInfo(
                    callId = callId,
                    veilederIdent = veilederIdent,
                    token = token,
                ).toVeilederinfoDTO()
                call.respond(veilederinfo)
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve Veilederinfo for NavIdent"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }
    }
}
