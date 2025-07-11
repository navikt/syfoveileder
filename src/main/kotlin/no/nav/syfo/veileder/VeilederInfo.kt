package no.nav.syfo.veileder

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
    val enabled: Boolean? = null,
)

// TODO: Erstatte med modifisert VeilederInfo?
data class Veileder(
    val givenName: String?,
    val surname: String?,
    val mail: String?,
    val businessPhones: String?, // FRom list (should only ever contain one) to string
    val accountEnabled: Boolean,
    val onPremisesSamAccountName: String?,
) {
    companion object {
        fun Veileder.toVeilederInfo() =
            VeilederInfo(
                ident = this.onPremisesSamAccountName ?: "",
                fornavn = this.givenName ?: "",
                etternavn = this.surname ?: "",
                epost = this.mail ?: "",
                telefonnummer = this.businessPhones,
                enabled = this.accountEnabled,
            )
    }
}

data class Gruppe(
    val id: String, // UUID
    val displayName: String,
    val description: String?,
    val onPremisesSamAccountName: String?,
)
