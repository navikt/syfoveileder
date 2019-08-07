package no.nav.syfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

data class Veileder(val ident: String,
                    val fornavn: String,
                    val etternavn: String,
                    val enhetNr: String,
                    val enhetNavn: String)

data class AADVeileder(
        val givenName: String,
        val surname: String,
        val onPremisesSamAccountName: String, // Ident - feks Z991234
        val streetAddress: String?, // Enhet nummer - feks 0315
        val city: String // Enhet navn - feks  Nav Grünerløkka
        )
data class EnhetResponse (
        val navn: String,
        val enhetNr: String
)

fun AADVeileder.toVeileder(): Veileder =
    Veileder(fornavn = givenName, etternavn = surname, ident = onPremisesSamAccountName,
            enhetNr = streetAddress ?:"0000", enhetNavn = city)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetUsersResponse( val value: List<AADVeileder>)

data class AADToken(
        val accessToken: String,
        val refreshToken: String?,
        val expires: LocalDateTime
)
