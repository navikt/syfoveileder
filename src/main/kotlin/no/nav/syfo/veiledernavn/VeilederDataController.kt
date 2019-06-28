package no.nav.syfo.veiledernavn

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.security.spring.oidc.validation.api.Unprotected
import no.nav.syfo.Veileder
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veiledere"])
class VeilederDataController @Inject constructor(val graphService: GraphService) {

    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(value = ["/enhet/{enhet}/enhetNavn/{enhetNavn}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentVeiledere(@PathVariable enhet: String, @PathVariable enhetNavn: String): List<Veileder> {
        return graphService.getVeiledere(enhetNr = enhet, enhetNavn = enhetNavn)
    }
}
