package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.azuread.AzureAdTokenResponse
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.testhelper.getRandomPort
import java.nio.file.Paths

fun wellKnownInternalAzureAD(): WellKnown {
    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    return WellKnown(
        issuer = "https://sts.issuer.net/veileder/v2",
        jwksUri = uri.toString()
    )
}

class AzureAdMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    private val azureAdTokenResponse = AzureAdTokenResponse(
        access_token = "token",
        expires_in = 3600,
        token_type = "type",
    )

    val name = "azuread"
    val server = mockAzureAdServer(port = port)

    private fun mockAzureAdServer(
        port: Int
    ): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port,
        ) {
            installContentNegotiation()
            routing {
                post {
                    call.respond(azureAdTokenResponse)
                }
            }
        }
    }
}
