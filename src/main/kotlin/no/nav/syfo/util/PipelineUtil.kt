package no.nav.syfo.util

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*

fun PipelineContext<out Unit, ApplicationCall>.getBearerHeader(): String? {
    return this.call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
}

fun PipelineContext<out Unit, ApplicationCall>.getCallId(): String {
    return this.call.request.headers[NAV_CALL_ID_HEADER].toString()
}

fun PipelineContext<out Unit, ApplicationCall>.getConsumerId(): String {
    return this.call.request.headers[NAV_CONSUMER_ID_HEADER].toString()
}
