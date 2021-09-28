package no.nav.syfo.veileder.api

data class VeilederInfoDTO(
    val ident: String,
    val navn: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String?,
)
