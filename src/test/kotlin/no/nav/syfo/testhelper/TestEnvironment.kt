package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import java.net.ServerSocket

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
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}
