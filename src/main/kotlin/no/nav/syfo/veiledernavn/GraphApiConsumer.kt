package no.nav.syfo.veiledernavn

import no.nav.syfo.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import javax.ws.rs.*

@Component
class GraphApiConsumer(
        private val aadTokenConsumer: AADTokenConsumer,
        private val restTemplate: RestTemplate,
        private val norg2Consumer: Norg2Consumer,
        @Value("\${graphapi.url}") val graphApiUrl: String
) {

    fun getVeiledere(
            enhetNr: String
    ): List<Veileder> {

        val token: AADToken = aadTokenConsumer.getAADToken()
        val enhetNavn = norg2Consumer.hentEnhetNavn(enhetNr)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        // Todo: Cache token i aadTokenConsumer, og la den fornye seg selv
        headers.set("Authorization", "Bearer " + aadTokenConsumer.renewTokenIfExpired(token).accessToken)

        val url = "${graphApiUrl}/v1.0/users/?${'$'}filter=city eq '$enhetNavn'&${'$'}select=onPremisesSamAccountName,givenName,surname,streetAddress,city"
        try {
            LOG.info("Azure Graph - get users - URL: '$url'")
            val responseEntity = restTemplate.exchange(url, GET, HttpEntity<Any>(headers), GetUsersResponse::class.java)
            return responseEntity
                    .body
                    ?.value
                    ?.filter { aadVeileder -> aadVeileder.streetAddress == enhetNr }
                    ?.map { aadVeileder -> aadVeileder.toVeileder() }
                    ?: throw RuntimeException("Svar fra Graph API har ikke forventet format.")

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