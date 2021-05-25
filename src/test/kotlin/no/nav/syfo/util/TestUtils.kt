package no.nav.syfo.util

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.oidc.context.*
import no.nav.security.oidc.test.support.JwtTokenGenerator
import no.nav.syfo.api.auth.OIDCClaim.NAVIDENT
import java.util.*
import java.util.concurrent.TimeUnit

object TestUtils {
    //OIDC-hack - legg til token og oidcclaims for en testbruker
    fun loggInnSomVeileder(oidcRequestContextHolder: OIDCRequestContextHolder, veilederIdent: String) {

        val claimsSet = buildClaimSet(subject = veilederIdent)
//        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        val issuer = OIDCIssuer.AZURE
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        oidcRequestContextHolder.setOIDCValidationContext(oidcValidationContext)
    }

    fun loggUt(oidcRequestContextHolder: OIDCRequestContextHolder) {
        oidcRequestContextHolder.setOIDCValidationContext(null)
    }

    fun buildClaimSet(
        subject: String?,
        issuer: String = JwtTokenGenerator.ISS,
        audience: String = JwtTokenGenerator.AUD,
        authLevel: String = JwtTokenGenerator.ACR,
        expiry: Long = JwtTokenGenerator.EXPIRY,
    ): JWTClaimsSet {
        val now = Date()
        return JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .audience(audience)
            .jwtID(UUID.randomUUID().toString())
            .claim("acr", authLevel)
            .claim("ver", "1.0")
            .claim("nonce", "myNonce")
            .claim("auth_time", now)
            .claim(NAVIDENT, subject)
            .notBeforeTime(now)
            .issueTime(now)
            .expirationTime(Date(now.time + expiry)).build()
    }
}
