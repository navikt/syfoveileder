package no.nav.syfo.veileder

import no.nav.syfo.veileder.api.VeilederDTO

data class Veileder(
    val ident: String,
    val fornavn: String?,
    val etternavn: String?,
)

fun List<Veileder>.toVeilederDTOList() = this.map {
    it.toVeilederDTO()
}

fun Veileder.toVeilederDTO() =
    VeilederDTO(
        ident = this.ident,
        fornavn = this.fornavn,
        etternavn = this.etternavn,
    )
