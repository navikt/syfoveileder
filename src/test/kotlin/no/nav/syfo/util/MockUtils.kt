package no.nav.syfo.util

import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators

object MockUtils {

    fun mockAxsysResponse(mockRestServiceServer: MockRestServiceServer) {
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), requestTo("https://axsys.url/v1/enhet/0123/brukere"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess()
                    .body(TestData.getAxsysVeiledereResponseBody)
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }

    fun mockAxsysEnhetsNummerFinnesIkke(mockRestServiceServer: MockRestServiceServer) {
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), requestTo("https://axsys.url/v1/enhet/0999/brukere"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withBadRequest()
                    .body(TestData.errorResponseBodyAxsys)
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }
}
