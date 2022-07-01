package no.nav.syfo.client.axsys

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientProxy
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class AxsysClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientProxy()

    private val axsysEnhetUrl: String = "$baseUrl$AXSYS_ENHET_BASE_PATH"

    suspend fun veilederList(
        callId: String,
        enhetNr: String,
        token: String,
    ): List<AxsysVeileder> {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken
            ?: throw RuntimeException("Failed to request list of Veiledere from Axsys: Failed to get system token from AzureAD")

        return try {
            val url = "$axsysEnhetUrl/$enhetNr$AXSYS_BRUKERE_PATH"

            val response: List<AxsysVeileder> = httpClient.get(url) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID_HEADER, callId)
                header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_APP_ID)
                accept(ContentType.Application.Json)
            }.body()
            COUNT_CALL_AXSYS_VEILEDER_LIST_SUCCESS.increment()
            response
        } catch (e: ResponseException) {
            COUNT_CALL_AXSYS_VEILEDER_LIST_FAIL.increment()
            log.error(
                "Error while requesting VeilederList from Axsys {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            throw e
        }
    }

    companion object {
        const val AXSYS_ENHET_BASE_PATH = "/api/v1/enhet"
        const val AXSYS_BRUKERE_PATH = "/brukere"

        private val log = LoggerFactory.getLogger(AxsysClient::class.java)
    }
}
