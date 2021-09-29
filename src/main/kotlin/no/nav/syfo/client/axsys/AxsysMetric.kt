package no.nav.syfo.client.axsys

import io.micrometer.core.instrument.Counter
import no.nav.syfo.metric.METRICS_NS
import no.nav.syfo.metric.METRICS_REGISTRY

const val CALL_AXSYS_BASE = "${METRICS_NS}_call_axsys"

const val CALL_AXSYS_VEILEDER_LIST_BASE = "${CALL_AXSYS_BASE}_veileder"
const val CALL_AXSYS_VEILEDER_LIST_SUCCESS = "${CALL_AXSYS_VEILEDER_LIST_BASE}_success_count"
const val CALL_AXSYS_VEILEDER_LIST_FAIL = "${CALL_AXSYS_VEILEDER_LIST_BASE}_fail_count"

val COUNT_CALL_AXSYS_VEILEDER_LIST_SUCCESS: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_SUCCESS)
    .description("Counts the number of successful calls to GraphAPI - VeilederList")
    .register(METRICS_REGISTRY)
val COUNT_CALL_AXSYS_VEILEDER_LIST_FAIL: Counter = Counter
    .builder(CALL_AXSYS_VEILEDER_LIST_FAIL)
    .description("Counts the number of failed calls to GraphAPI - VeilederList")
    .register(METRICS_REGISTRY)
