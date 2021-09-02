package no.nav.syfo.api.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.util.OIDCIssuer
import java.text.ParseException

fun getSubjectInternAzureV2(contextHolder: TokenValidationContextHolder): String {
    val context = contextHolder.tokenValidationContext
    return try {
        context.getClaims(OIDCIssuer.VEILEDER_AZURE_V2).getStringClaim(OIDCClaim.NAVIDENT)
    } catch (e: ParseException) {
        throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
    }
}

fun getOIDCToken(contextHolder: TokenValidationContextHolder, issuer: String): String {
    return contextHolder.tokenValidationContext.getJwtToken(issuer).tokenAsString
}
