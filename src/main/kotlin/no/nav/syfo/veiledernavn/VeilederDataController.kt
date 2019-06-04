package no.nav.syfo.veiledernavn

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.security.spring.oidc.validation.api.Unprotected
import no.nav.syfo.Veileder
import no.nav.syfo.util.OIDCIssuer.AZURE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/veileder"])
class VeilederDataController @Inject constructor(val virksomhetEnhetConsumer: VirksomhetEnhetConsumer) {

    @ProtectedWithClaims(issuer = AZURE)
    @Unprotected
    @GetMapping(value = ["/{ident}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentVeilederNavn(@PathVariable ident: String): Veileder {
        return virksomhetEnhetConsumer.hentVeilederInfo(ident)
    }
}
