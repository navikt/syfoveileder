package no.nav.syfo.testhelper

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.veilederinfo.GraphApiGetUserResponse
import no.nav.syfo.veilederinfo.GraphApiUser
import org.springframework.http.*
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators

val emptyGraphApiGetUserResponse = GraphApiGetUserResponse(
    value = emptyList()
)

val graphApiGetUserResponse = GraphApiGetUserResponse(
    value = listOf(
        GraphApiUser(
            givenName = "Given",
            surname = "Suname",
            mailNickname = "Z999999",
            mail = "give.surname@nav.no",
            businessPhones = emptyList(),
        )
    )
)

fun mockGetUsersResponse(
    mockRestServiceServer: MockRestServiceServer,
    response: GraphApiGetUserResponse? = graphApiGetUserResponse
) {
    val responseBody = ObjectMapper().writeValueAsString(
        response?.copy() ?: emptyGraphApiGetUserResponse.copy()
    )
    mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.anything())
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer ${aadToken.accessToken}"))
        .andRespond(
            MockRestResponseCreators.withSuccess()
                .body(responseBody)
                .contentType(MediaType.APPLICATION_JSON)
        )
}
