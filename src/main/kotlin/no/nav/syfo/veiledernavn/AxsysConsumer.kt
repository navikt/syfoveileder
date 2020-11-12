package no.nav.syfo.veiledernavn

import no.nav.syfo.*
import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import javax.ws.rs.*
import org.springframework.core.ParameterizedTypeReference


@Component
class AxsysConsumer(
    private val metric: Metric,
    private val restTemplate: RestTemplate,
    @Value("\${axsys.url}") val axsysUrl: String
) {

    fun getAxsysVeiledere(
            enhetNr: String
    ): List<AxsysVeileder> {
        val headers = HttpHeaders()

        headers.set("Nav-Call-Id", "default")
        headers.set("Nav-Consumer-Id", "srvsyfoveileder")
        headers.contentType = MediaType.APPLICATION_JSON

        val url = "${axsysUrl}/v1/enhet/$enhetNr/brukere"
        try {
            val responseEntity = restTemplate.exchange(
                    url,
                    GET,
                    HttpEntity<Any>(headers),
                    typeReference<List<AxsysVeileder>>()
            )
            responseEntity.body?.let{
                metric.countEvent(CALL_AXSYS_VEILEDERE_SUCCESS)
                return it
            } ?: throw RuntimeException("Svar fra Axsys API har ikke forventet format.")

        } catch (e: HttpClientErrorException) {
            LOG.warn("Oppslag i Axsys feiler med respons ${e.responseBodyAsString}", e)
            metric.countEvent(CALL_AXSYS_VEILEDERE_FAIL)
            throw BadRequestException("Oppslag i Axsys API feiler pga feil i request", e)
        } catch (e: HttpServerErrorException) {
            LOG.error("Server feil fra Axsys API ${e.responseBodyAsString}", e)
            metric.countEvent(CALL_AXSYS_VEILEDERE_FAIL)
            throw ServiceUnavailableException("Serverfeil fra Axsys API ved henting av veiledere på enhet")
        } catch (e: java.lang.RuntimeException) {
            val runtimeMelding = "RunTimeException på henting av veiledere på enhet i Axsys API"
            LOG.error(runtimeMelding, e)
            throw InternalServerErrorException(runtimeMelding)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AxsysConsumer::class.java.name)

        private const val CALL_AXSYS_VEILEDERE_BASE = "call_axsys_veiledere"
        private const val CALL_AXSYS_VEILEDERE_FAIL = "${CALL_AXSYS_VEILEDERE_BASE}_fail"
        private const val CALL_AXSYS_VEILEDERE_SUCCESS = "${CALL_AXSYS_VEILEDERE_BASE}_success"
    }
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
