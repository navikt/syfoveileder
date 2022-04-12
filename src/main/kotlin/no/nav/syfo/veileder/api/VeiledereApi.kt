package no.nav.syfo.veileder.api

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.util.getBearerHeader
import no.nav.syfo.util.getCallId
import no.nav.syfo.veileder.toVeilederDTOList
import no.nav.syfo.veiledernavn.VeilederService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo")

const val apiVeiledereBasePath = "/syfoveileder/api/v2/veiledere"
const val apiVeiledereEnhetPath = "/enhet"
const val apiVeiledereEnhetParam = "enhetNr"

fun Route.registrerVeiledereApi(
    veilederService: VeilederService,
) {
    route(apiVeiledereBasePath) {
        get("$apiVeiledereEnhetPath/{$apiVeiledereEnhetParam}") {
            val callId = getCallId()
            try {
                val token = getBearerHeader()
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val enhetNr = call.parameters[apiVeiledereEnhetParam]
                    ?: throw IllegalArgumentException("No EnhetNr found in path param")

                val veilederList = veilederService.getVeiledere(
                    callId = callId,
                    enhetNr = enhetNr,
                    token = token,
                ).toVeilederDTOList()
                call.respond(veilederList)
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve VeilederList for enhetNr:"
                log.warn("$illegalArgumentMessage: {}, {}", e.message, callId)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }
    }
}
