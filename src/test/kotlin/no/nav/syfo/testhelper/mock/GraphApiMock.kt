package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
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

fun generateGraphapiUserListResponse() =
    BatchResponse(
        responses = listOf(
            BatchBody(
                id = "1",
                body = GetUsersResponse(
                    value = listOf(
                        AADVeileder(
                            givenName = "Given",
                            surname = "Surname",
                            onPremisesSamAccountName = VEILEDER_IDENT,
                        ),
                    ),
                ),
            ),
        ),
    )

class GraphApiMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val graphapiUserResponse = generateGraphapiUserResponse()
    val graphapiUserListResponse = generateGraphapiUserListResponse()

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
                    call.respond(graphapiUserResponse)
                }
                post("/v1.0/*") {
                    call.respond(graphapiUserListResponse)
                }
            }
        }
    }
}
