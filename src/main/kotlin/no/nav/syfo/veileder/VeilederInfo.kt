package no.nav.syfo.veileder

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val enabled: Boolean? = null,
)

data class Gruppe(
    val uuid: String,
    val adGruppenavn: String,
)
