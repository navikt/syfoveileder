package no.nav.syfo.client.graphapi

import io.ktor.client.*
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
import org.slf4j.LoggerFactory

class GraphApiClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val cache: RedisStore,
    private val httpClient: HttpClient = httpClientProxy(),
) {

    suspend fun veileder(
        callId: String,
        veilederIdent: String,
        token: String,
    ): GraphApiUser? {
        val cachedObject: GraphApiUser? = cache.getObject(cacheKey(veilederIdent))
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT.increment()
            cachedObject
        } else {
            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = baseUrl,
                token = token,
            )?.accessToken ?: throw RuntimeException("Failed to request access to Veileder in Graph API: Failed to get OBO token")

            getVeilederGraphApiUser(veilederIdent = veilederIdent, token = oboToken, callId = callId)
        }
    }

    suspend fun veilederMedSystemToken(
        callId: String,
        token: String,
        veilederIdent: String,
    ): GraphApiUser? {
        val cachedObject: GraphApiUser? = cache.getObject(cacheKey(veilederIdent))
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT.increment()
            cachedObject
        } else {
            val systemToken = azureAdClient.getSystemToken(
                token = token,
                scopeClientId = baseUrl,
            )?.accessToken ?: throw RuntimeException("Failed to request access to Veileder in Graph API: Failed to get system token")

            getVeilederGraphApiUser(veilederIdent = veilederIdent, token = systemToken, callId = callId)
        }
    }

    private suspend fun getVeilederGraphApiUser(
        veilederIdent: String,
        token: String,
        callId: String
    ) = try {
        val queryFilter = "startsWith(onPremisesSamAccountName, '$veilederIdent')"
        val queryFilterWhitespaceEncoded = queryFilter.replace(" ", "%20")
        val url =
            "$baseUrl/v1.0/users?\$filter=$queryFilterWhitespaceEncoded&\$select=onPremisesSamAccountName,givenName,surname,mail,businessPhones,accountEnabled&\$count=true"

        val response: GraphApiGetUserResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, bearerHeader(token))
            header("ConsistencyLevel", "eventual")
            accept(ContentType.Application.Json)
        }.body()
        COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS.increment()
        val graphAPIUser = response.value.firstOrNull()
        if (graphAPIUser != null) {
            cache.setObject(
                expireSeconds = CACHE_EXPIRATION_SECONDS,
                key = cacheKey(veilederIdent),
                value = graphAPIUser,
            )
        }
        COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_MISS.increment()
        graphAPIUser
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

    suspend fun veilederList(
        enhetNr: String,
        axsysVeilederlist: List<AxsysVeileder>,
        callId: String,
        token: String,
    ): List<GraphApiUser> {
        val cacheKey = "$GRAPH_API_CACHE_VEILEDERE_FRA_ENHET_PREFIX$enhetNr"
        val cachedObject: List<GraphApiUser>? = cache.getListObject(cacheKey)
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_HIT.increment()
            cachedObject
        } else {
            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = baseUrl,
                token = token,
            )?.accessToken ?: throw RuntimeException("Failed to request access to Enhet: Failed to get OBO token")

            val identChunks = axsysVeilederlist.chunked(15)
            val url = "$baseUrl/v1.0/\$batch"

            val requests = identChunks.mapIndexed { index, idents ->
                val query =
                    idents.joinToString(separator = " or ") { "startsWith(onPremisesSamAccountName, '${it.appIdent}')" }
                RequestEntry(
                    id = (index + 1).toString(),
                    method = "GET",
                    url = "/users?\$filter=$query&\$select=onPremisesSamAccountName,givenName,surname,mail,businessPhones,accountEnabled&\$count=true",
                    headers = mapOf("ConsistencyLevel" to "eventual", "Content-Type" to "application/json")
                )
            }

            try {
                val responseData = requests.chunked(20).flatMap { requestList ->
                    val requestBody = GraphBatchRequest(requestList)

                    val response: BatchResponse = httpClient.post(url) {
                        accept(ContentType.Application.Json)
                        header(HttpHeaders.Authorization, bearerHeader(oboToken))
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }.body()
                    response.responses.flatMap { it.body.value }
                }
                COUNT_CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS.increment()
                cache.setObject(
                    expireSeconds = CACHE_EXPIRATION_SECONDS,
                    key = cacheKey,
                    value = responseData,
                )
                COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_MISS.increment()
                return responseData
            } catch (e: ResponseException) {
                log.error(
                    "Error while requesting VeilederList from GraphApi {}, {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    StructuredArguments.keyValue("message", e.message),
                    callIdArgument(callId),
                )
                COUNT_CALL_GRAPHAPI_VEILEDER_LIST_FAIL.increment()
                throw e
            }
        }
    }

    companion object {
        const val GRAPH_API_CACHE_VEILEDER_PREFIX = "graphapiVeileder-"
        const val GRAPH_API_CACHE_VEILEDERE_FRA_ENHET_PREFIX = "graphapiVeiledereFraEnhet-"

        private const val CACHE_EXPIRATION_SECONDS = (60 * 60 * 12).toLong()
        private val log = LoggerFactory.getLogger(GraphApiClient::class.java)

        private fun cacheKey(veilederIdent: String) = "$GRAPH_API_CACHE_VEILEDER_PREFIX$veilederIdent"
    }
}
