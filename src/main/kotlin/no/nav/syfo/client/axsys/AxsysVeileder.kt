package no.nav.syfo.client.axsys

import no.nav.syfo.veileder.VeilederInfo

data class AxsysVeileder(
    val appIdent: String,
    val historiskIdent: Number,
)

fun AxsysVeileder.toVeilederInfo() =
    VeilederInfo(
        ident = appIdent,
        fornavn = "",
        etternavn = "",
        epost = "",
        telefonnummer = "",
    )
