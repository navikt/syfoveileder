package no.nav.syfo.client.graphapi

import no.nav.syfo.veileder.VeilederInfo

data class GraphApiUser(
    val givenName: String,
    val surname: String,
    val onPremisesSamAccountName: String,
    val accountEnabled: Boolean,
)

data class GraphApiGetUserResponse(
    val value: List<GraphApiUser>,
)

fun GraphApiUser.toVeilederInfo(veilederIdent: String) =
    VeilederInfo(
        ident = veilederIdent,
        fornavn = this.givenName,
        etternavn = this.surname,
        enabled = this.accountEnabled,
    )
