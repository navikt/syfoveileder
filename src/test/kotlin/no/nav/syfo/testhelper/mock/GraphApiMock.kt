package no.nav.syfo.testhelper.mock

import com.microsoft.graph.models.Group
import com.microsoft.graph.models.GroupCollectionResponse
import com.microsoft.graph.models.User
import com.microsoft.graph.models.UserCollectionResponse
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.client.graphapi.*
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_2

val veilederUser = GraphApiUser(
    givenName = "Given",
    surname = "Surname",
    onPremisesSamAccountName = VEILEDER_IDENT,
    mail = "give.surname@nav.no",
    businessPhones = emptyList(),
    accountEnabled = true,
)

val veilederDisabledUser = veilederUser.copy(
    accountEnabled = false,
    onPremisesSamAccountName = VEILEDER_IDENT_2
)

fun generateGraphapiUserResponseEmpty() =
    GraphApiGetUserResponse(
        value = emptyList()
    )

fun generateGraphapiUserListResponse(): BatchResponse {
    return BatchResponse(
        responses = listOf(
            BatchBody(
                id = "1",
                body = GetUsersResponse(
                    value = listOf(veilederUser),
                ),
            ),
        ),
    )
}

val graphapiUserResponse = GraphApiGetUserResponse(
    value = listOf(veilederUser)
)
val graphapiDisabledUserResponse = GraphApiGetUserResponse(
    value = listOf(veilederDisabledUser)
)

fun MockRequestHandleScope.graphApiMockResponse(request: HttpRequestData): HttpResponseData {
    return when (request.method) {
        HttpMethod.Get -> {
            val filter = request.url.parameters["\$filter"]!!
            val response =
                if (filter.contains(VEILEDER_IDENT)) graphapiUserResponse else if (filter.contains(VEILEDER_IDENT_2)) graphapiDisabledUserResponse else generateGraphapiUserResponseEmpty()
            respond(response)
        }

        HttpMethod.Post -> {
            respond(generateGraphapiUserListResponse())
        }

        else -> respondBadRequest()
    }
}

fun user(): User {
    val user = User()
    user.givenName = "Given"
    user.surname = "Surname"
    user.onPremisesSamAccountName = VEILEDER_IDENT
    user.mail = "give.surname@nav.no"
    // TODO: Add phone
    user.businessPhones = emptyList()
    user.accountEnabled = true
    return user
}

fun group(): Group {
    val group = Group()
    group.id = "123"
    group.displayName = "Group 123"
    group.description = "Group 123"
    group.onPremisesSamAccountName = "GROUP_ID"
    return group
}

fun groupCollectionResponse() {
    val collectionResponse = GroupCollectionResponse()
    collectionResponse.value = listOf(group())
    collectionResponse
}

fun userCollectionResponse() {
    val collectionResponse = UserCollectionResponse()
    collectionResponse.value = listOf(user())
    collectionResponse
}

fun MockRequestHandleScope.graphApiGrupperMockResponse(request: HttpRequestData): HttpResponseData {
    return when (request.method) {
        HttpMethod.Get -> respond(groupCollectionResponse())
        else -> respondBadRequest()
    }
}

fun MockRequestHandleScope.graphApiMeMemberOfMockResponse(request: HttpRequestData): HttpResponseData {
    return when (request.method) {
        HttpMethod.Get -> respond(userCollectionResponse())
        else -> respondBadRequest()
    }
}
