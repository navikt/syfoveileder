package no.nav.syfo.util

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments
import java.util.*

const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val NAV_CONSUMER_APP_ID = "syfoveileder"

fun bearerHeader(token: String): String {
    return "Bearer $token"
}

fun createCallId(): String = UUID.randomUUID().toString()

fun callIdArgument(callId: String): StructuredArgument = StructuredArguments.keyValue("callId", callId)
