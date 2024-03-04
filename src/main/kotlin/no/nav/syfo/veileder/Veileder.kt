package no.nav.syfo.veileder

import no.nav.syfo.veileder.api.VeilederDTO

data class Veileder(
    val ident: String,
    val fornavn: String?,
    val etternavn: String?,
) {
    companion object {
        fun fromVeilederInfo(veilederInfo: VeilederInfo): Veileder =
            Veileder(
                ident = veilederInfo.ident,
                fornavn = veilederInfo.fornavn,
                etternavn = veilederInfo.etternavn,
            )
    }
}

fun List<Veileder>.toVeilederDTOList() = this.map {
    it.toVeilederDTO()
}

fun Veileder.toVeilederDTO() =
    VeilederDTO(
        ident = this.ident,
        fornavn = this.fornavn,
        etternavn = this.etternavn,
    )
