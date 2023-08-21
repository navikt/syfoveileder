package no.nav.syfo.client.graphapi

import io.micrometer.core.instrument.Counter
import no.nav.syfo.metric.METRICS_NS
import no.nav.syfo.metric.METRICS_REGISTRY

const val CALL_GRAPHAPI_BASE = "${METRICS_NS}_call_graphapi"

const val CALL_GRAPHAPI_VEILEDER_BASE = "${CALL_GRAPHAPI_BASE}_veileder"
const val CALL_GRAPHAPI_VEILEDER_SUCCESS = "${CALL_GRAPHAPI_VEILEDER_BASE}_success_count"
const val CALL_GRAPHAPI_VEILEDER_FAIL = "${CALL_GRAPHAPI_VEILEDER_BASE}_fail_count"
const val CALL_GRAPHAPI_VEILEDER_CACHE_HIT = "${CALL_GRAPHAPI_VEILEDER_BASE}_cache_hit"
const val CALL_GRAPHAPI_VEILEDER_CACHE_MISS = "${CALL_GRAPHAPI_VEILEDER_BASE}_cache_miss"

val COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_SUCCESS)
    .description("Counts the number of successful calls to GraphAPI - Veileder")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_FAIL: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_FAIL)
    .description("Counts the number of failed calls to GraphAPI - Veileder")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_CACHE_HIT)
    .description("Counts the number of cache hits GraphAPI - Veileder")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_MISS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_CACHE_MISS)
    .description("Counts the number of cache miss GraphAPI - Veileder")
    .register(METRICS_REGISTRY)
