package no.nav.syfo.veilederinfo

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
)

fun VeilederInfo.toVeilederDTO() =
    VeilederInfoDTO(
        ident = this.ident,
        navn = "${this.fornavn} ${this.etternavn}",
        fornavn = this.fornavn,
        etternavn = this.etternavn,
        epost = this.epost,
        telefonnummer = this.telefonnummer,
    )
