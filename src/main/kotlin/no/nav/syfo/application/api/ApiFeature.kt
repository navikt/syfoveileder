package no.nav.syfo.application.api

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import no.nav.syfo.application.api.exception.RestException
import no.nav.syfo.metric.METRICS_REGISTRY
import no.nav.syfo.util.NAV_CALL_ID_HEADER
import no.nav.syfo.util.configure
import no.nav.syfo.util.getCallId
import no.nav.syfo.util.getConsumerId
import java.time.Duration
import java.util.*

fun Application.installMetrics() {
    install(MicrometerMetrics) {
        registry = METRICS_REGISTRY
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .maximumExpectedValue(Duration.ofSeconds(20).toNanos().toDouble())
            .build()
    }
}

fun Application.installCallId() {
    install(CallId) {
        retrieve { it.request.headers[NAV_CALL_ID_HEADER] }
        generate { UUID.randomUUID().toString() }
        verify { callId: String -> callId.isNotEmpty() }
        header(NAV_CALL_ID_HEADER)
    }
}

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        jackson {
            configure()
        }
    }
}

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")
            val callId = call.getCallId()
            val consumerId = call.getConsumerId()
            when (cause) {
                is ChannelWriteException ->
                    call.application.log.warn("Caught exception, callId=$callId, consumerId=$consumerId", cause)

                is RestException -> call.application.log.error(
                    "${cause.prefixMessage}: callId=$callId, statusCode=${cause.statusCode}, message=${cause.message}",
                    cause
                )

                else ->
                    call.application.log.error("Caught exception, callId=$callId, consumerId=$consumerId", cause)
            }
            throw cause
        }
    }
}
