package no.nav.syfo.veileder

import com.microsoft.graph.models.User

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
    val enabled: Boolean? = null,
)

fun User.toVeilederInfo() =
    VeilederInfo(
        ident = this.onPremisesSamAccountName,
        fornavn = this.givenName,
        etternavn = this.surname,
        epost = this.mail ?: "",
        telefonnummer = this.businessPhones.firstOrNull(),
        enabled = this.accountEnabled,
    )
