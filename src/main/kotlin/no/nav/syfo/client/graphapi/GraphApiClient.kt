package no.nav.syfo.client.graphapi

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientProxy
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.callIdArgument
import no.nav.syfo.veileder.Veileder
import org.slf4j.LoggerFactory

class GraphApiClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
) {
    private val httpClient = httpClientProxy()

    suspend fun veileder(
        callId: String,
        veilederIdent: String,
        token: String,
    ): GraphApiGetUserResponse {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = baseUrl,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Enhet: Failed to get OBO token")

        return try {
            val queryFilter = "startsWith(mailNickname, '$veilederIdent')"
            val url =
                "$baseUrl/v1.0//users/?\$filter=$queryFilter&\$select=mailNickname,givenName,surname,mail,businessPhones"

            val response: GraphApiGetUserResponse = httpClient.get(url) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                accept(ContentType.Application.Json)
            }
            COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS.increment()
            response
        } catch (e: ResponseException) {
            COUNT_CALL_GRAPHAPI_VEILEDER_FAIL.increment()
            log.error(
                "Error while requesting Veileder from GraphApi {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            throw e
        }
    }

    suspend fun veilederList(
        axsysVeilederlist: List<AxsysVeileder>,
        callId: String,
        token: String,
    ): List<Veileder> {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = baseUrl,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Enhet: Failed to get OBO token")

        val identChunks = axsysVeilederlist.chunked(15)
        val url = "$baseUrl/v1.0/\$batch"

        val requests = identChunks.mapIndexed { index, idents ->
            val query = idents.joinToString(separator = " or ") { "startsWith(mailNickname, '${it.appIdent}')" }
            RequestEntry(
                id = (index + 1).toString(),
                method = "GET",
                url = "/users/?\$filter=$query&\$select=mailNickname,givenName,surname",
                headers = mapOf("Content-Type" to "application/json")
            )
        }

        try {
            val responseData = requests.chunked(20).flatMap { requestList ->
                val requestBody = GraphBatchRequest(requestList)

                val response: BatchResponse = httpClient.post(url) {
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    contentType(ContentType.Application.Json)
                    body = requestBody
                }
                response.responses
                    .flatMap { batchBody ->
                        batchBody.body.value.map { aadVeileder ->
                            aadVeileder.toVeileder()
                        }
                    }
            }
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS.increment()
            return responseData
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting VeilederList from GraphApi {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_FAIL.increment()
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GraphApiClient::class.java)
    }
}
