package no.nav.syfo.client.azuread

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenCredential
import com.microsoft.graph.serviceclient.GraphServiceClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.syfo.application.api.authentication.getConsumerClientId
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.httpClientProxy
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.ZoneOffset

class AzureAdClient(
    private val azureAppClientId: String,
    private val azureAppClientSecret: String,
    private val azureOpenidConfigTokenEndpoint: String,
    private val graphApiUrl: String,
    private val cache: ValkeyStore,
    private val httpClient: HttpClient = httpClientProxy(),
) {

    suspend fun getOnBehalfOfToken(
        scopeClientId: String,
        token: String,
    ): AzureAdToken? {
        val azp = getConsumerClientId(token)
        val veilederIdent = getNAVIdentFromToken(token)
        val cacheKey = "$veilederIdent-$azp-$scopeClientId"
        val cachedOnBehalfOfToken: AzureAdToken? = cache.getObject(cacheKey)
        return if (cachedOnBehalfOfToken?.isExpired() == false) {
            COUNT_CALL_AZUREAD_TOKEN_OBO_CACHE_HIT.increment()
            cachedOnBehalfOfToken
        } else {
            val azureAdTokenResponse = getAccessToken(
                Parameters.build {
                    append("client_id", azureAppClientId)
                    append("client_secret", azureAppClientSecret)
                    append("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    append("assertion", token)
                    append("scope", scope(scopeClientId))
                    append("requested_token_use", "on_behalf_of")
                }
            )
            azureAdTokenResponse?.let {
                it.toAzureAdToken().also { azureAdToken ->
                    cache.setObject(
                        expireSeconds = 3600,
                        key = cacheKey,
                        value = azureAdToken,
                    )
                    COUNT_CALL_AZUREAD_TOKEN_OBO_CACHE_MISS.increment()
                }
            }
        }
    }

    suspend fun getSystemToken(scopeClientId: String, token: String): AzureAdToken? {
        val azp = getConsumerClientId(token)
        val cacheKey = "azuread-systemtoken-$azp-$scopeClientId"
        val cachedSystemToken: AzureAdToken? = cache.getObject(key = cacheKey)
        return if (cachedSystemToken?.isExpired() == false) {
            COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_HIT.increment()
            cachedSystemToken
        } else {
            val azureAdTokenResponse = getAccessToken(
                formParameters = Parameters.build {
                    append("client_id", azureAppClientId)
                    append("client_secret", azureAppClientSecret)
                    append("grant_type", "client_credentials")
                    append("scope", scope(scopeClientId))
                },
            )

            azureAdTokenResponse?.toAzureAdToken().also { azureAdToken ->
                cache.setObject(
                    expireSeconds = 3600,
                    key = cacheKey,
                    value = azureAdToken
                )
                COUNT_CALL_AZUREAD_TOKEN_SYSTEM_CACHE_MISS.increment()
            }
        }
    }

    fun createGraphServiceClient(azureAdToken: AzureAdToken): GraphServiceClient {
        val atOffset = azureAdToken.expires.atOffset(ZoneOffset.UTC)
        val tokenCredential = TokenCredential(
            function = {
                Mono.just(AccessToken(azureAdToken.accessToken, atOffset))
            }
        )

        // TODO: Sjekke at scopes blir riktig
        val scopes = arrayOf("https://graph.microsoft.com/.default")
        return GraphServiceClient(tokenCredential, scopes.toString())
    }

    private suspend fun getAccessToken(
        formParameters: Parameters,
    ): AzureAdTokenResponse? {
        return try {
            val response: HttpResponse = httpClient.post(azureOpenidConfigTokenEndpoint) {
                accept(ContentType.Application.Json)
                setBody(FormDataContent(formParameters))
            }
            response.body<AzureAdTokenResponse>()
        } catch (e: ClientRequestException) {
            handleUnexpectedResponseException(e)
            null
        } catch (e: ServerResponseException) {
            handleUnexpectedResponseException(e)
            null
        }
    }

    private fun handleUnexpectedResponseException(
        responseException: ResponseException,
    ) {
        log.error(
            "Error while requesting AzureAdAccessToken with statusCode=${responseException.response.status.value}",
            responseException
        )
    }

    private fun scope(scopeClientId: String): String {
        return if (scopeClientId == graphApiUrl) {
            "$scopeClientId/.default"
        } else {
            "api://$scopeClientId/.default"
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AzureAdClient::class.java)
    }
}
