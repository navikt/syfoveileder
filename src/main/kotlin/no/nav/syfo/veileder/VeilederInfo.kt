package no.nav.syfo.veileder

import no.nav.syfo.veileder.api.VeilederInfoDTO

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
)

fun VeilederInfo.toVeilederinfoDTO() =
    VeilederInfoDTO(
        ident = this.ident,
        navn = "${this.fornavn} ${this.etternavn}",
        fornavn = this.fornavn,
        etternavn = this.etternavn,
        epost = this.epost,
        telefonnummer = this.telefonnummer,
    )
