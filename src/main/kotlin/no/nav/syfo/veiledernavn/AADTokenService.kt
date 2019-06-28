package no.nav.syfo.veiledernavn

import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.AuthenticationResult
import com.microsoft.aad.adal4j.ClientCredential
import no.nav.syfo.AADToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Future

@Component
class AADTokenService(
        @Value("\${aadaccesstoken.url}") val aadUrl: String,
        @Value("\${aad_syfoveileder_clientid.username}") val clientId: String,
        @Value("\${aad_syfoveileder_clientid.password}") val clientSecret: String,
        private val context: AuthenticationContext
){

    fun renewTokenIfExpired(token: AADToken): AADToken =
        if (token.expires.isBefore(LocalDateTime.now().minusMinutes(2L))) {
            LOG.debug("Azure - Renewing token" )
            getAADToken()
        } else {
            token
        }

    fun getAADToken(): AADToken {
        val futureToken: Future<AuthenticationResult> = context
                .acquireToken(aadUrl, ClientCredential(clientId, clientSecret), null)

        val result: AuthenticationResult = futureToken.get()

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
