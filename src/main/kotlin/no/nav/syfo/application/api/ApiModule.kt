package no.nav.syfo.application.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.JwtIssuer
import no.nav.syfo.application.api.authentication.JwtIssuerType
import no.nav.syfo.application.api.authentication.installJwtAuthentication
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.veileder.api.registrerVeilederSystemApi
import no.nav.syfo.veileder.api.registrerVeiledereApi
import no.nav.syfo.veiledernavn.VeilederService

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
    veilederService: VeilederService,
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

    routing {
        registerPodApi(
            applicationState = applicationState,
        )
        registerPrometheusApi()
        authenticate(JwtIssuerType.INTERNAL_AZUREAD.name) {
            registrerVeiledereApi(veilederService = veilederService)
            registrerVeilederSystemApi(veilederService = veilederService, preAuthorizedApps = environment.preAuthorizedApps)
        }
    }
}
