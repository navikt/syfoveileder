package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.testhelper.UserConstants.ENHET_NR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_2
import no.nav.syfo.testhelper.getRandomPort

fun generateAxsysResponse() = listOf(
    AxsysVeileder(
        appIdent = VEILEDER_IDENT,
        historiskIdent = 123456789,
    ),
    AxsysVeileder(
        appIdent = VEILEDER_IDENT_2,
        historiskIdent = 987654321,
    ),
)

class AxsysMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val axsysResponse = generateAxsysResponse()

    val name = "axsys"
    val server = mockAxsysServer()

    private fun mockAxsysServer(): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            installContentNegotiation()
            routing {
                get("/v1/enhet/$ENHET_NR/brukere") {
                    call.respond(axsysResponse)
                }
            }
        }
    }
}
