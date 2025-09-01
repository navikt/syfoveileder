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

const val CALL_MS_GRAPH_API_GRUPPE_BASE = "${CALL_GRAPHAPI_BASE}_gruppe"
const val CALL_MS_GRAPH_API_GRUPPE_SUCCESS = "${CALL_MS_GRAPH_API_GRUPPE_BASE}_success_count"
const val CALL_MS_GRAPH_API_GRUPPE_FAIL = "${CALL_MS_GRAPH_API_GRUPPE_BASE}_fail_count"
const val CALL_MS_GRAPH_API_GRUPPE_CACHE_HIT = "${CALL_MS_GRAPH_API_GRUPPE_BASE}_cache_hit"
const val CALL_MS_GRAPH_API_GRUPPE_CACHE_MISS = "${CALL_MS_GRAPH_API_GRUPPE_BASE}_cache_miss"

val COUNT_CALL_MS_GRAPH_API_GRUPPE_SUCCESS: Counter = Counter
    .builder(CALL_MS_GRAPH_API_GRUPPE_SUCCESS)
    .description("Counts the number of successful calls to Microsoft Graph API - Gruppe")
    .register(METRICS_REGISTRY)
val COUNT_CALL_MS_GRAPH_API_GRUPPE_FAIL: Counter = Counter
    .builder(CALL_MS_GRAPH_API_GRUPPE_FAIL)
    .description("Counts the number of failed calls to Microsoft Graph API - Gruppe")
    .register(METRICS_REGISTRY)
val COUNT_CALL_MS_GRAPH_API_GRUPPE_CACHE_HIT: Counter = Counter
    .builder(CALL_MS_GRAPH_API_GRUPPE_CACHE_HIT)
    .description("Counts the number of cache hits Microsoft Graph API - Gruppe")
    .register(METRICS_REGISTRY)
val COUNT_CALL_MS_GRAPH_API_GRUPPE_CACHE_MISS: Counter = Counter
    .builder(CALL_MS_GRAPH_API_GRUPPE_CACHE_MISS)
    .description("Counts the number of cache miss Microsoft Graph API - Gruppe")
    .register(METRICS_REGISTRY)

const val CALL_GRAPHAPI_VEILEDER_LIST_BASE = "${CALL_GRAPHAPI_BASE}_veileder_liste"
const val CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_success_count"
const val CALL_GRAPHAPI_VEILEDER_LIST_FAIL = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_fail_count"
const val CALL_GRAPHAPI_VEILEDER_LIST_CACHE_HIT = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_cache_hit"
const val CALL_GRAPHAPI_VEILEDER_LIST_CACHE_MISS = "${CALL_GRAPHAPI_VEILEDER_LIST_BASE}_cache_miss"

val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS)
    .description("Counts the number of successful calls to GraphAPI - Veilederliste")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_FAIL: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_FAIL)
    .description("Counts the number of failed calls to GraphAPI - Veilederliste")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_HIT: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_CACHE_HIT)
    .description("Counts the number of cache hits GraphAPI - Veilederliste")
    .register(METRICS_REGISTRY)
val COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_MISS: Counter = Counter
    .builder(CALL_GRAPHAPI_VEILEDER_LIST_CACHE_MISS)
    .description("Counts the number of cache miss GraphAPI - Veilederliste")
    .register(METRICS_REGISTRY)
