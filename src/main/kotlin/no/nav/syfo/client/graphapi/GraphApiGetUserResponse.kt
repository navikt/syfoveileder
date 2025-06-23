package no.nav.syfo.client.graphapi

import no.nav.syfo.veileder.VeilederInfo

data class GraphApiUser(
    val givenName: String,
    val surname: String,
    val onPremisesSamAccountName: String,
    val mail: String?,
    val businessPhones: List<String>?,
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
        epost = this.mail ?: "",
        telefonnummer = this.businessPhones?.firstOrNull(),
        enabled = this.accountEnabled,
    )

data class User(
    val givenName: String,
    val surname: String,
    val onPremisesSamAccountName: String,
    val mail: String,
    val businessPhones: List<String>,
    val accountEnabled: Boolean,
)

data class Group(
    val id: String,
    val displayName: String,
    val onPremisesSamAccountName: String,
    val description: String?,
    val members: List<User>,
)

data class UserResponse(
    val value: List<Group>,
)

fun User.toVeilederInfo() =
    VeilederInfo(
        ident = this.onPremisesSamAccountName,
        fornavn = this.givenName,
        etternavn = this.surname,
        epost = this.mail,
        telefonnummer = this.businessPhones.firstOrNull(),
        enabled = this.accountEnabled,
    )
