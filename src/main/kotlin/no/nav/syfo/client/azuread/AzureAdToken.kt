package no.nav.syfo.client.azuread

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenCredential
import reactor.core.publisher.Mono
import java.io.Serializable
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

data class AzureAdToken(
    val accessToken: String,
    val expires: LocalDateTime,
) : Serializable {

    fun toTokenCredential(): TokenCredential {
        return TokenCredential { Mono.just(AccessToken(accessToken, expires.toOffsetDateTimeUTC())) }
    }
}

fun LocalDateTime.toOffsetDateTimeUTC(): OffsetDateTime =
    this.atZone(ZoneId.of("Europe/Oslo")).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime()

fun AzureAdToken.isExpired() = this.expires < LocalDateTime.now().plusSeconds(60)
