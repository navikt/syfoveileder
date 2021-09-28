package no.nav.syfo.client.axsys

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class AxsysClient(
    private val baseUrl: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun veilederList(
        callId: String,
        enhetNr: String
    ): List<AxsysVeileder> {
        return try {
            val url = "$baseUrl/v1/enhet/$enhetNr/brukere"

            val response: List<AxsysVeileder> = httpClient.get(url) {
                header(NAV_CALL_ID_HEADER, callId)
                header(NAV_CONSUMER_ID_HEADER, NAV_CONSUMER_APP_ID)
                accept(ContentType.Application.Json)
            }
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
        private val log = LoggerFactory.getLogger(AxsysClient::class.java)
    }
}
