package no.nav.syfo.util

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.test.JwtTokenGenerator
import java.util.*

object TestUtils {
    fun loggInnSomVeilederV2(
        tokenValidationContextHolder: TokenValidationContextHolder,
        veilederIdent: String,
    ) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)

        val jwtToken = JwtToken(jwt.serialize())
        val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
        issuerTokenMap[OIDCIssuer.VEILEDER_AZURE_V2] = jwtToken
        val tokenValidationContext = TokenValidationContext(issuerTokenMap)
        tokenValidationContextHolder.tokenValidationContext = tokenValidationContext
    }

    fun loggInnSomVeileder(
        tokenValidationContextHolder: TokenValidationContextHolder,
        veilederIdent: String,
    ) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)

        val jwtToken = JwtToken(jwt.serialize())
        val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
        issuerTokenMap[OIDCIssuer.AZURE] = jwtToken
        val tokenValidationContext = TokenValidationContext(issuerTokenMap)
        tokenValidationContextHolder.tokenValidationContext = tokenValidationContext
    }

    fun loggUt(tokenValidationContextHolder: TokenValidationContextHolder) {
        tokenValidationContextHolder.tokenValidationContext = null
    }
}
