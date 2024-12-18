package no.nav.syfo.application

import io.ktor.server.application.*
import no.nav.syfo.application.cache.RedisConfig
import java.net.URI

data class Environment(
    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureOpenidConfigTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),

    val axsysClientId: String = getEnvVar("AXSYS_CLIENT_ID"),
    val axsysUrl: String = getEnvVar("AXSYS_URL"),
    val graphapiUrl: String = getEnvVar("GRAPHAPI_URL"),
    val redisConfig: RedisConfig = RedisConfig(
        redisUri = URI(getEnvVar("REDIS_URI_CACHE")),
        redisDB = 24, // se https://github.com/navikt/istilgangskontroll/blob/master/README.md
        redisUsername = getEnvVar("REDIS_USERNAME_CACHE"),
        redisPassword = getEnvVar("REDIS_PASSWORD_CACHE"),
    ),
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

val Application.envKind get() = environment.config.property("ktor.environment").getString()

fun Application.isDev(block: () -> Unit) {
    if (envKind == "dev") block()
}

fun Application.isProd(block: () -> Unit) {
    if (envKind == "production") block()
}
