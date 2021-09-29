package no.nav.syfo.client.graphapi

import io.micrometer.core.instrument.Counter
import no.nav.syfo.metric.METRICS_NS
import no.nav.syfo.metric.METRICS_REGISTRY

const val CALL_GRAPHAPI_BASE = "${METRICS_NS}_call_graphapi"

const val CALL_GRAPHAPI_VEILEDER_BASE = "${CALL_GRAPHAPI_BASE}_veileder"
const val CALL_GRAPHAPI_VEILEDER_SUCCESS = "${CALL_GRAPHAPI_VEILEDER_BASE}_success_count"
const val CALL_GRAPHAPI_VEILEDER_FAIL = "${CALL_GRAPHAPI_VEILEDER_BASE}_fail_count"

val COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_SUCCESS)
    .description("Counts the number of successful calls to GraphAPI - Veileder")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_FAIL: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_FAIL)
    .description("Counts the number of failed calls to GraphAPI - Veileder")
    .register(METRICS_REGISTRY)

const val CALL_GRAPHAPI_VEILEDER_LIST_BASE = "${CALL_GRAPHAPI_BASE}_veileder"
const val CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_success_count"
const val CALL_GRAPHAPI_VEILEDER_LIST_FAIL = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_fail_count"

val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS)
    .description("Counts the number of successful calls to GraphAPI - VeilederList")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_FAIL: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_FAIL)
    .description("Counts the number of failed calls to GraphAPI - VeilederList")
    .register(METRICS_REGISTRY)
