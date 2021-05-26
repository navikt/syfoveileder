package no.nav.syfo.testhelper

import no.nav.syfo.AADToken
import no.nav.syfo.veiledernavn.AADTokenConsumer
import org.mockito.BDDMockito
import java.time.LocalDateTime

val aadToken = AADToken(
    accessToken = "token",
    refreshToken = "refreshtoken",
    expires = LocalDateTime.parse("2019-01-01T10:00:00"),
)

fun mockAADTokenConsumer(
    aadTokenService: AADTokenConsumer,
) {
    BDDMockito.given(aadTokenService.getAADToken()).willReturn(aadToken)
    BDDMockito.given(aadTokenService.renewTokenIfExpired(aadToken)).willReturn(
        aadToken,
    )
}
