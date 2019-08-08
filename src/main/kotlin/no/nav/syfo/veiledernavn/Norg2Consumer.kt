package no.nav.syfo.veiledernavn

import no.nav.syfo.EnhetResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import javax.ws.rs.*

@Component
class Norg2Consumer(
        private val restTemplate: RestTemplate,
        @Value("\${norg2.url}") val norg2url: String
) {

    // TODO: cache denne i en dag
    fun hentEnhetNavn(enhetNr: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "${norg2url}/enhet/${enhetNr}"

        try {
            Norg2Consumer.LOG.info("Norg2 - get enhet - URL: '$url'")
            val enhet = restTemplate.getForObject(url, EnhetResponse::class.java)
            return enhet?.navn ?: throw HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: HttpClientErrorException) {
            Norg2Consumer.LOG.warn("Henting av enhetsnavn i Norg2 feiler med respons ${e.responseBodyAsString}", e)
            throw BadRequestException("Oppslag i Norg2 har feil i request", e)
        } catch (e: HttpServerErrorException) {
            Norg2Consumer.LOG.error("Serverfeil fra Norg2 ${e.responseBodyAsString}", e)
            throw ServiceUnavailableException("Serverfeil fra Norg2 ved henting av enhetsnavn")
        } catch (e: RuntimeException) {
            val runtimeMelding = "RunTimeException ved henting av enhetsnavn i Norg2"
            Norg2Consumer.LOG.error(runtimeMelding, e)
            throw InternalServerErrorException(runtimeMelding)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Norg2Consumer::class.java.name)
    }
}


