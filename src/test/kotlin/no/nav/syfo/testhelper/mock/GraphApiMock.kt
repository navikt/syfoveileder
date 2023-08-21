package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.graphapi.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.getRandomPort

fun generateGraphapiUserResponse() =
    GraphApiGetUserResponse(
        value = listOf(
            GraphApiUser(
                givenName = "Given",
                surname = "Surname",
                onPremisesSamAccountName = VEILEDER_IDENT,
                mail = "give.surname@nav.no",
                businessPhones = emptyList(),
            )
        )
    )

fun generateGraphapiUserResponseEmpty() =
    GraphApiGetUserResponse(
        value = emptyList()
    )

class GraphApiMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val graphapiUserResponse = generateGraphapiUserResponse()
    val graphapiUserResponseEmpty = generateGraphapiUserResponseEmpty()

    val name = "graphapi"
    val server = mockEregServer()

    private fun mockEregServer(): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            installContentNegotiation()
            routing {
                get("/v1.0/*") {
                    val filter = this.call.request.queryParameters.get("\$filter")
                    call.respond(
                        if (filter!!.contains(VEILEDER_IDENT)) graphapiUserResponse else graphapiUserResponseEmpty
                    )
                }
            }
        }
    }
}
