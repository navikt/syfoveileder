package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.veiledernavn.VeilederService

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
) {
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        graphApiUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.redisCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val axsysClient = AxsysClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.axsysUrl,
        clientId = externalMockEnvironment.environment.axsysClientId,
        cache = externalMockEnvironment.redisCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val graphApiClient = GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.redisCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val veilederService = VeilederService(
        axsysClient = axsysClient,
        graphApiClient = graphApiClient,
    )

    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        veilederService = veilederService,
    )
}
