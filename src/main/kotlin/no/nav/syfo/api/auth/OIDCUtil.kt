package no.nav.syfo.api.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.util.OIDCIssuer
import java.text.ParseException

fun getSubjectInternAzure(contextHolder: TokenValidationContextHolder): String {
    val context = contextHolder.tokenValidationContext
    return try {
        context.getClaims(OIDCIssuer.AZURE).getStringClaim(OIDCClaim.NAVIDENT)
    } catch (e: ParseException) {
        throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
    }
}
