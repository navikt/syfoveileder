package no.nav.syfo.veiledernavn

import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.ClientCredential
import no.nav.syfo.AADToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Executors

@Component
class AADTokenService(
        @Value("\${graphapi.url}") val resource: String, // resource er url-en hvor tokenet er gyldig
        @Value("\${aad_syfoveileder_clientid.username}") val clientId: String,
        @Value("\${aad_syfoveileder_clientid.password}") val clientSecret: String,
        @Value("\${aadauthority.url}") val authority: String
){

    fun renewTokenIfExpired(token: AADToken): AADToken =
        if (token.expires.isBefore(LocalDateTime.now().minusMinutes(2L))) {
            LOG.debug("Azure - Renewing token" )
            getAADToken()
        } else {
            token
        }

    fun getAADToken(): AADToken {
        val service = Executors.newFixedThreadPool(1)
        val context = AuthenticationContext(authority, true, service)
        val result = context.acquireToken(resource, ClientCredential(clientId, clientSecret), null).get()

        return AADToken(
                result.accessToken,
                result.refreshToken,
                result.expiresOnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AADTokenService::class.java.name)
    }
}
