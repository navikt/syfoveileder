package no.nav.syfo.client.axsys

import no.nav.syfo.veileder.Veileder

data class AxsysVeileder(
    val appIdent: String,
    val historiskIdent: Number,
)

fun AxsysVeileder.toVeileder() =
    Veileder(
        fornavn = "",
        etternavn = "",
        ident = appIdent,
    )
