package no.nav.syfo.testhelper

import no.nav.syfo.consumer.azuread.AzureAdV2Token
import no.nav.syfo.consumer.azuread.AzureAdV2TokenConsumer
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito
import java.time.LocalDateTime

val aadToken = AzureAdV2Token(
    accessToken = "token",
    expires = LocalDateTime.parse("2019-01-01T10:00:00"),
)

fun mockAADTokenConsumer(
    azureAdV2TokenService: AzureAdV2TokenConsumer,
) {
    BDDMockito.given(azureAdV2TokenService.getToken(anyString())).willReturn(aadToken)
}
