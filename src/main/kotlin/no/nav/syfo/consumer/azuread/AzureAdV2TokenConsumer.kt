package no.nav.syfo.consumer.azuread

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.getOIDCToken
import no.nav.syfo.util.OIDCIssuer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Component
class AzureAdV2TokenConsumer(
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    @Value("\${azure.app.client.id}") private val azureAppClientId: String,
    @Value("\${azure.app.client.secret}") private val azureAppClientSecret: String,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String,
    @Value("\${graphapi.url}") val graphApiUrl: String,
    private val contextHolder: TokenValidationContextHolder
) {
    fun getToken(
        scopeClientId: String,
    ): AzureAdV2Token {
        val token = getOIDCToken(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        try {
            val requestEntity = onBehalfOfRequestEntity(
                scopeClientId = scopeClientId,
                token = token,
            )
            val response = restTemplateWithProxy.exchange(
                azureTokenEndpoint,
                HttpMethod.POST,
                requestEntity,
                AzureAdV2TokenResponse::class.java,
            )
            val tokenResponse = response.body!!

            return tokenResponse.toAzureAdV2Token()
        } catch (e: RestClientResponseException) {
            log.error("Call to get AzureADV2Token from AzureAD for scope: $scopeClientId with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}", e)
            throw e
        }
    }

    private fun onBehalfOfRequestEntity(
        scopeClientId: String,
        token: String
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("client_secret", azureAppClientSecret)
        body.add("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("assertion", token)
        body.add("requested_token_use", "on_behalf_of")
        if (scopeClientId == graphApiUrl) {
            body.add("scope", "$scopeClientId/.default")
        } else {
            body.add("scope", "api://$scopeClientId/.default")
        }
        return HttpEntity<MultiValueMap<String, String>>(body, headers)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AzureAdV2TokenConsumer::class.java.name)
    }
}
