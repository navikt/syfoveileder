package no.nav.syfo.veiledernavn

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import javax.ws.rs.BadRequestException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.ServiceUnavailableException

@Component
class GraphApiConsumer(
        private val aadTokenConsumer: AADTokenConsumer,
        private val restTemplate: RestTemplate,
        @Value("\${graphapi.url}") val graphApiUrl: String
) {

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
            return requests.chunked(20).flatMap { requests ->
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

        } catch (e: HttpClientErrorException) {
            LOG.warn("Oppslag i Graph API feiler med respons ${e.responseBodyAsString}", e)
            throw BadRequestException("Oppslag i Graph API feiler pga feil i request", e)
        } catch (e: HttpServerErrorException) {
            LOG.error("Server feil fra Graph API ${e.responseBodyAsString}", e)
            throw ServiceUnavailableException("Serverfeil fra Graph Api ved henting av veilederinformasjon")
        } catch (e: java.lang.RuntimeException) {
            val runtimeMelding = "RunTimeException på henting av veilederinformasjon i Graph API"
            LOG.error(runtimeMelding, e)
            throw InternalServerErrorException(runtimeMelding)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphApiConsumer::class.java.name)
    }
}

