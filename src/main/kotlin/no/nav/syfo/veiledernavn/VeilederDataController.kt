package no.nav.syfo.veiledernavn

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.Veileder
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veiledere"])
class VeilederDataController @Inject constructor(
    private val metric: Metric,
    private val veilederService: VeilederService
) {
    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(value = ["/enhet/{enhet}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVeiledere(@PathVariable enhet: String): List<Veileder> {
        metric.countIncomingRequests("enhet_veiledere")
        return veilederService.getVeiledere(enhetNr = enhet)
    }
}
