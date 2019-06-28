package no.nav.syfo.veiledernavn

import no.nav.syfo.AADToken
import no.nav.syfo.GetUsersResponse
import no.nav.syfo.Veileder
import no.nav.syfo.toVeileder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class GraphService(
        private val AADTokenService: AADTokenService,
        private val restTemplate: RestTemplate,
        @Value("\${graphApi.url}") val graphApiUrl: String
) {

    fun getVeiledere(
            enhetNr: String,
            enhetNavn: String,
            token: AADToken = AADTokenService.getAADToken()
    ): List<Veileder>{
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        // Todo: Cache token in AADTokenService, and let it renew itself
        headers.set("Authorization", "Bearer " + AADTokenService.renewTokenIfExpired(token).accessToken)
        val url = "${graphApiUrl}/v1.0/users/?${'$'}filter=city eq '$enhetNavn'&${'$'}select=onPremisesSamAccountName,givenName,surname,streetAddress,city"
        try {
            LOG.debug("Azure Graph - get users - URL: '$url'")
            val responseEntity = restTemplate.exchange(url, GET, HttpEntity<Any>(headers), GetUsersResponse::class.java)
            if (responseEntity.statusCode != OK) {
                val message = "Kall mot Graph Apiet feiler med HTTP statuskode" + responseEntity.statusCode
                LOG.error(message)
                throw RuntimeException(message) // Todo: Fiks feilmeldingshåndtering
            }
           return responseEntity
                   .body
                   ?.value
                   ?.filter { aadVeileder -> aadVeileder.streetAddress == enhetNr}
                   ?.map { aadVeileder -> aadVeileder.toVeileder()}
                   ?: throw RuntimeException("Svar fra Graph API har ikke forventet format.")

        } catch (e: HttpClientErrorException) {
            LOG.error("Feil ved oppslag i Graph API", e)
            throw RuntimeException(e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphService::class.java.name)
    }
}
