package no.nav.syfo.veiledernavn

import no.nav.syfo.*
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.callIdArgument
import no.nav.syfo.veilederinfo.GraphApiGetUserResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import javax.ws.rs.*

@Component
class GraphApiConsumer(
    private val aadTokenConsumer: AADTokenConsumer,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    @Value("\${graphapi.url}") val graphApiUrl: String
) {
    fun veileder(
        callId: String,
        veilederIdent: String
    ): GraphApiGetUserResponse {
        val token: AADToken = aadTokenConsumer.getAADToken()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(aadTokenConsumer.renewTokenIfExpired(token).accessToken)

        try {
            val queryFilter = "startsWith(mailNickname, '${veilederIdent}')"
            val url = "${graphApiUrl}/v1.0//users/?\$filter=${queryFilter}&\$select=mailNickname,givenName,surname,mail,businessPhones"
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<String>(headers),
                GraphApiGetUserResponse::class.java,
            )
            val responseBody = response.body!!
            metric.countEvent(CALL_GRAPHAPI_VEILEDER_SUCCESS)
            return responseBody
        } catch (e: RestClientResponseException) {
            LOG.error(
                "Call to get response from Microsoft Graph failed with status: {} and message: {}. {}",
                e.rawStatusCode,
                e.responseBodyAsString,
                callIdArgument(callId),
            )
            metric.countEvent(CALL_GRAPHAPI_VEILEDER_FAIL)
            throw e
        }
    }

    fun getVeiledere(
            axysVeileders: List<AxsysVeileder>
    ): List<Veileder> {
        val token: AADToken = aadTokenConsumer.getAADToken()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        // Todo: Cache token i aadTokenConsumer, og la den fornye seg selv
        headers.set("Authorization", "Bearer " + aadTokenConsumer.renewTokenIfExpired(token).accessToken)

        val identChunks = axysVeileders.chunked(15)

        val url = "${graphApiUrl}/v1.0/\$batch"

        val requests = identChunks.mapIndexed { index, idents ->
            val query = idents.joinToString(separator = " or ") { "startsWith(mailNickname, '${it.appIdent}')" }
            RequestEntry(
                    id = (index + 1).toString(),
                    method = "GET",
                    url = "/users/?\$filter=${query}&\$select=mailNickname,givenName,surname",
                    headers = mapOf("Content-Type" to "application/json")
            )
        }

        try {
            val responseEnitty = requests.chunked(20).flatMap { requests ->
                val body = GraphBatchRequest(requests)
                val responseEntity = restTemplate.exchange(url, HttpMethod.POST, HttpEntity(body, headers), BatchResponse::class.java)

                responseEntity
                        .body
                        ?.responses
                        ?.flatMap {
                            it.body.value.map { aadVeileder -> aadVeileder.toVeileder() }
                        }
                        ?: throw RuntimeException("Svar fra Graph API har ikke forventet format.")
            }
            metric.countEvent(CALL_GRAPHAPI_VEILEDERE_SUCCESS)
            return responseEnitty

        } catch (e: HttpClientErrorException) {
            LOG.warn("Oppslag i Graph API feiler med respons ${e.responseBodyAsString}", e)
            metric.countEvent(CALL_GRAPHAPI_VEILEDERE_FAIL)
            throw BadRequestException("Oppslag i Graph API feiler pga feil i request", e)
        } catch (e: HttpServerErrorException) {
            LOG.error("Server feil fra Graph API ${e.responseBodyAsString}", e)
            metric.countEvent(CALL_GRAPHAPI_VEILEDERE_FAIL)
            throw ServiceUnavailableException("Serverfeil fra Graph Api ved henting av veilederinformasjon")
        } catch (e: java.lang.RuntimeException) {
            val runtimeMelding = "RunTimeException på henting av veilederinformasjon i Graph API"
            LOG.error(runtimeMelding, e)
            throw InternalServerErrorException(runtimeMelding)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphApiConsumer::class.java.name)

        private const val CALL_GRAPHAPI_VEILEDER_BASE = "call_graphapi_veileder"
        private const val CALL_GRAPHAPI_VEILEDER_FAIL = "${CALL_GRAPHAPI_VEILEDER_BASE}_fail"
        private const val CALL_GRAPHAPI_VEILEDER_SUCCESS = "${CALL_GRAPHAPI_VEILEDER_BASE}_success"

        private const val CALL_GRAPHAPI_VEILEDERE_BASE = "call_graphapi_veiledere"
        private const val CALL_GRAPHAPI_VEILEDERE_FAIL = "${CALL_GRAPHAPI_VEILEDERE_BASE}_fail"
        private const val CALL_GRAPHAPI_VEILEDERE_SUCCESS = "${CALL_GRAPHAPI_VEILEDERE_BASE}_success"
    }
}

