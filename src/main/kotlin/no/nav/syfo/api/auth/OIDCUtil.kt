package no.nav.syfo.api.auth

import no.nav.security.oidc.OIDCConstants
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.syfo.util.OIDCIssuer
import java.text.ParseException

fun getSubjectInternAzure(contextHolder: OIDCRequestContextHolder): String {
    val context = contextHolder
        .getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
    return try {
        context.getClaims(OIDCIssuer.AZURE).claimSet.getStringClaim(OIDCClaim.NAVIDENT)
    } catch (e: ParseException) {
        throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
    }
}
