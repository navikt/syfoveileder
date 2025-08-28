package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.PreAuthorizedApp
import no.nav.syfo.application.cache.ValkeyConfig
import java.net.URI

fun testEnvironment() = Environment(
    azureAppClientId = "isdialogmote-client-id",
    azureAppClientSecret = "isdialogmote-secret",
    azureAppWellKnownUrl = "wellknown",
    azureOpenidConfigTokenEndpoint = "azureOpenIdTokenEndpoint",
    preAuthorizedApps = testAzureAppPreAuthorizedApps,
    graphapiUrl = "graphapiUrl",
    valkeyConfig = ValkeyConfig(
        valkeyUri = URI("http://localhost:6379"),
        valkeyDB = 0,
        valkeyUsername = "valkeyUser",
        valkeyPassword = "valkeyPassword",
        ssl = false,
    )
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

private const val syfooversiktsrvApplicationName: String = "syfooversiktsrv"
const val syfooversiktsrvClientId = "$syfooversiktsrvApplicationName-client-id"

val testAzureAppPreAuthorizedApps = listOf(
    PreAuthorizedApp(
        name = "cluster:teamsykefravr:$syfooversiktsrvApplicationName",
        clientId = syfooversiktsrvClientId,
    )
)
