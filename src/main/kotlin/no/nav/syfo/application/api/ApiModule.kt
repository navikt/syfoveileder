package no.nav.syfo.application.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.veileder.api.registrerVeiledereApi
import no.nav.syfo.veileder.api.registrerVeiledereApiV3
import no.nav.syfo.veileder.api.registrerVeilederinfoApi
import no.nav.syfo.veiledernavn.VeilederService

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
    cache: RedisStore,
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
        cache = cache,
    )
    val axsysClient = AxsysClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.axsysUrl,
        clientId = environment.axsysClientId,
        cache = cache,
    )
    val graphApiClient = GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.graphapiUrl,
        cache = cache,
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
            registrerVeiledereApiV3(veilederService = veilederService)
        }
    }
}
