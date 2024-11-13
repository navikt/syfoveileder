package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.cache.RedisConfig
import java.net.ServerSocket
import java.net.URI

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    axsysUrl: String,
    graphapiUrl: String,
) = Environment(
    azureAppClientId = "isdialogmote-client-id",
    azureAppClientSecret = "isdialogmote-secret",
    azureAppWellKnownUrl = "wellknown",
    azureOpenidConfigTokenEndpoint = azureOpenIdTokenEndpoint,
    axsysClientId = "dev-fss.org.axsys",
    axsysUrl = axsysUrl,
    graphapiUrl = graphapiUrl,
    redisConfig = RedisConfig(
        redisUri = URI("http://localhost:6379"),
        redisDB = 0,
        redisUsername = "redisUser",
        redisPassword = "redisPassword",
        ssl = false,
    )
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}
