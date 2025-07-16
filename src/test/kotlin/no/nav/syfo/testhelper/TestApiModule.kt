package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.veiledernavn.VeilederService

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
    graphApiClientMock: GraphApiClient? = null
) {
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        graphApiUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.valkeyCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val graphApiClient = graphApiClientMock ?: GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.graphapiUrl,
        cache = externalMockEnvironment.valkeyCache,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val veilederService = VeilederService(
        graphApiClient = graphApiClient,
    )

    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        veilederService = veilederService,
    )
}
