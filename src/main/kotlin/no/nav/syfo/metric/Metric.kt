package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class Metric @Inject constructor(
    private val registry: MeterRegistry
) {
    fun countEvent(navn: String) {
        registry.counter(
            addPrefix(navn),
            Tags.of("type", "info")
        ).increment()
    }

    fun countIncomingRequests(name: String) {
        registry.counter(
            addPrefix("request_incoming_$name"),
            Tags.of("type", "info")
        ).increment()
    }

    private fun addPrefix(navn: String): String {
        val metricPrefix = "syfoveileder_"
        return metricPrefix + navn
    }
}
