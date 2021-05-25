package no.nav.syfo.veiledernavn.api.v2

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.Veileder
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.veiledernavn.VeilederService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RestController
@RequestMapping(value = ["/api/v2/veiledere"])
class VeilederDataControllerV2 @Inject constructor(
    private val metric: Metric,
    private val veilederService: VeilederService
) {
    @GetMapping(value = ["/enhet/{enhet}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVeiledere(@PathVariable enhet: String): List<Veileder> {
        metric.countIncomingRequests("enhet_veiledere")
        return veilederService.getVeiledere(enhetNr = enhet)
    }
}
