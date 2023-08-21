package no.nav.syfo.client.graphapi

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
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
    private val cache: RedisStore,
) {
    private val httpClient = httpClientProxy()

    suspend fun veileder(
        callId: String,
        veilederIdent: String,
        token: String,
    ): GraphApiGetUserResponse {
        val cacheKey = "graphapi-$veilederIdent"
        val cachedObject: GraphApiGetUserResponse? = cache.getObject(cacheKey)
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT.increment()
            cachedObject
        } else {
            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = baseUrl,
                token = token,
            )?.accessToken ?: throw RuntimeException("Failed to request access to Enhet: Failed to get OBO token")

            try {
                val queryFilter = "startsWith(onPremisesSamAccountName, '$veilederIdent')"
                val queryFilterWhitespaceEncoded = queryFilter.replace(" ", "%20")
                val url =
                    "$baseUrl/v1.0/users?\$filter=$queryFilterWhitespaceEncoded&\$select=onPremisesSamAccountName,givenName,surname,mail,businessPhones&\$count=true"

                val response: GraphApiGetUserResponse = httpClient.get(url) {
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    header("ConsistencyLevel", "eventual")
                    accept(ContentType.Application.Json)
                }.body()
                COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS.increment()
                cache.setObject(
                    expireSeconds = 3600,
                    key = cacheKey,
                    value = response,
                )
                COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_MISS.increment()
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
    }

    suspend fun veilederList(
        axsysVeilederlist: List<AxsysVeileder>,
        callId: String,
        token: String,
    ): List<Veileder> =
        axsysVeilederlist.map { Pair(it.appIdent, veileder(callId, it.appIdent, token)) }.mapNotNull { it.second.value.firstOrNull()?.toVeileder(it.first) }

    companion object {
        private val log = LoggerFactory.getLogger(GraphApiClient::class.java)
    }
}
