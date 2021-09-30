package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.veileder.api.registrerVeiledereApi
import no.nav.syfo.veileder.api.registrerVeilederinfoApi
import no.nav.syfo.veiledernavn.VeilederService

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
) {
    installMetrics()
    installCallId()
    installContentNegotiation()
    installJwtAuthentication(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.azureAppClientId),
                jwtIssuerType = JwtIssuerType.INTERNAL_AZUREAD,
                wellKnown = wellKnownInternalAzureAD,
            ),
        ),
    )
    installStatusPages()

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        graphApiUrl = environment.graphapiUrl,
    )
    val axsysClient = AxsysClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.isproxyUrl,
        isproxyClientId = environment.isproxyClientId,
    )
    val graphApiClient = GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.graphapiUrl,
    )

    val veilederService = VeilederService(
        axsysClient = axsysClient,
        graphApiClient = graphApiClient,
    )

    routing {
        registerPodApi(
            applicationState = applicationState,
        )
        registerPrometheusApi()
        authenticate(JwtIssuerType.INTERNAL_AZUREAD.name) {
            registrerVeilederinfoApi(
                veilederService = veilederService,
            )
            registrerVeiledereApi(
                veilederService = veilederService,
            )
        }
    }
}
