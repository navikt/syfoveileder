package no.nav.syfo.veiledernavn

import no.nav.syfo.AADToken
import no.nav.syfo.GetUsersResponse
import no.nav.syfo.Veileder
import no.nav.syfo.toVeileder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.stereotype.Component
import org.springframework.web.client.*
import javax.ws.rs.*

@Component
class GraphService(
        private val tokenService: AADTokenService,
        private val restTemplate: RestTemplate,
        @Value("\${graphapi.url}") val graphApiUrl: String
) {

    fun getVeiledere(
            enhetNr: String,
            enhetNavn: String,
            token: AADToken = tokenService.getAADToken()
    ): List<Veileder>{
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        // Todo: Cache token i tokenService, og la den fornye seg selv
        headers.set("Authorization", "Bearer " + tokenService.renewTokenIfExpired(token).accessToken)

        val url = "${graphApiUrl}/v1.0/users/?${'$'}filter=city eq '$enhetNavn'&${'$'}select=onPremisesSamAccountName,givenName,surname,streetAddress,city"
        try {
            LOG.info("Azure Graph - get users - URL: '$url'")
            val responseEntity = restTemplate.exchange(url, GET, HttpEntity<Any>(headers), GetUsersResponse::class.java)
            /*if (!responseEntity.statusCode.is2xxSuccessful()){
                val message = "Kall mot Graph Apiet feiler med HTTP statuskode" + responseEntity.statusCode
                LOG.error(message)
                throw RuntimeException(message)
            }*/
           return responseEntity
                   .body
                   ?.value
                   ?.filter { aadVeileder -> aadVeileder.streetAddress == enhetNr}
                   ?.map { aadVeileder -> aadVeileder.toVeileder()}
                   ?: throw RuntimeException("Svar fra Graph API har ikke forventet format.")

        } catch (e: HttpClientErrorException) {
            LOG.warn("Oppslag i Graph API feiler med respons ${e.responseBodyAsString}", e)
            throw BadRequestException("Oppslag i Graph API feiler pga feil i request", e)
        } catch (e: HttpServerErrorException) {
            LOG.error ("Server feil fra Graph API ${e.responseBodyAsString}", e)
            throw ServiceUnavailableException ("Serverfeil fra Graph Api ved henting av veilederinformasjon" )
        } catch (e: java.lang.RuntimeException) {
            val runtimeMelding = "RunTimeException på henting av veilederinformasjon i Graph API"
            LOG.error(runtimeMelding, e)
            throw InternalServerErrorException(runtimeMelding)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphService::class.java.name)
    }
}
