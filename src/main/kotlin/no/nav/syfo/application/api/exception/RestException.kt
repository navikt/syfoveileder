package no.nav.syfo.application.api.exception

import com.microsoft.kiota.ApiException

class RestException(
    val prefixMessage: String? = null,
    val statusCode: String? = null,
    message: String?,
    cause: Throwable
) : Exception(message, cause) {
    constructor(prefixMessage: String, e: ApiException) : this(
        prefixMessage = prefixMessage,
        statusCode = e.responseStatusCode.toString(),
        message = e.message,
        cause = e
    )

    constructor(prefixMessage: String, e: Exception) : this(
        prefixMessage = prefixMessage,
        message = e.message,
        cause = e
    )
}

fun Exception.toRestException(errorMessage: String): RestException =
    when (this) {
        is ApiException -> RestException(errorMessage, this)
        else -> RestException(errorMessage, this)
    }
