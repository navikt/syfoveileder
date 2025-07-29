package no.nav.syfo.application

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.application.cache.ValkeyConfig
import no.nav.syfo.util.configuredJacksonMapper
import java.net.URI

data class Environment(
    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureOpenidConfigTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val preAuthorizedApps: List<PreAuthorizedApp> = configuredJacksonMapper().readValue(getEnvVar("AZURE_APP_PRE_AUTHORIZED_APPS")),

    val graphapiUrl: String = getEnvVar("GRAPHAPI_URL"),
    val valkeyConfig: ValkeyConfig = ValkeyConfig(
        valkeyUri = URI(getEnvVar("VALKEY_URI_CACHE")),
        valkeyDB = 24, // se https://github.com/navikt/istilgangskontroll/blob/master/README.md
        valkeyUsername = getEnvVar("VALKEY_USERNAME_CACHE"),
        valkeyPassword = getEnvVar("VALKEY_PASSWORD_CACHE"),
    ),
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

data class PreAuthorizedApp(
    val name: String,
    val clientId: String
) {
    fun getAppnavn(): String {
        val split = name.split(":")
        return split[2]
    }
}
