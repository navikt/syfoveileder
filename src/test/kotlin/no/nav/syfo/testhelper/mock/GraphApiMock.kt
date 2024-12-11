package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.client.graphapi.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT

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

fun generateGraphapiUserListResponse() =
    BatchResponse(
        responses = listOf(
            BatchBody(
                id = "1",
                body = GetUsersResponse(
                    value = listOf(
                        GraphApiUser(
                            givenName = "Given",
                            surname = "Surname",
                            onPremisesSamAccountName = VEILEDER_IDENT,
                            mail = "give.surname@nav.no",
                            businessPhones = emptyList(),
                        ),
                    ),
                ),
            ),
        ),
    )

val graphapiUserResponse = generateGraphapiUserResponse()
val graphapiUserResponseEmpty = generateGraphapiUserResponseEmpty()
val graphapiUserListResponse = generateGraphapiUserListResponse()

fun MockRequestHandleScope.graphApiMockResponse(request: HttpRequestData): HttpResponseData {
    return when (request.method) {
        HttpMethod.Get -> {
            val filter = request.url.parameters["\$filter"]!!
            val response = if (filter.contains(VEILEDER_IDENT)) graphapiUserResponse else graphapiUserResponseEmpty
            respond(response)
        }
        HttpMethod.Post -> {
            respond(graphapiUserListResponse)
        }
        else -> respondBadRequest()
    }
}
