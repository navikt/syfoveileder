package no.nav.syfo.client.axsys

import io.micrometer.core.instrument.Counter
import no.nav.syfo.metric.METRICS_NS
import no.nav.syfo.metric.METRICS_REGISTRY

const val CALL_AXSYS_BASE = "${METRICS_NS}_call_axsys"

const val CALL_AXSYS_VEILEDER_LIST_BASE = "${CALL_AXSYS_BASE}_veileder"
const val CALL_AXSYS_VEILEDER_LIST_SUCCESS = "${CALL_AXSYS_VEILEDER_LIST_BASE}_success_count"
const val CALL_AXSYS_VEILEDER_LIST_FAIL = "${CALL_AXSYS_VEILEDER_LIST_BASE}_fail_count"
const val CALL_AXSYS_VEILEDER_LIST_CACHE_HIT = "${CALL_AXSYS_VEILEDER_LIST_BASE}_cache_hit"
const val CALL_AXSYS_VEILEDER_LIST_CACHE_MISS = "${CALL_AXSYS_VEILEDER_LIST_BASE}_cache_miss"

val COUNT_CALL_AXSYS_VEILEDER_LIST_SUCCESS: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_SUCCESS)
    .description("Counts the number of successful calls to Axsys - VeilederList")
    .register(METRICS_REGISTRY)
val COUNT_CALL_AXSYS_VEILEDER_LIST_FAIL: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_FAIL)
    .description("Counts the number of failed calls to Axsys - VeilederList")
    .register(METRICS_REGISTRY)
val COUNT_CALL_AXSYS_VEILEDER_CACHE_HIT: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_CACHE_HIT)
    .description("Counts the number of cache hits Axsys - VeilederList")
    .register(METRICS_REGISTRY)
val COUNT_CALL_AXSYS_VEILEDER_CACHE_MISS: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_CACHE_MISS)
    .description("Counts the number of cache miss Axsys - VeilederList")
    .register(METRICS_REGISTRY)
