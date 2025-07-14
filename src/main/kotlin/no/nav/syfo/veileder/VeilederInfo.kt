package no.nav.syfo.veileder

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
    val enabled: Boolean? = null,
)

// TODO: Erstatte med VeilederInfo? Flere felter kan være null og bør de filtreres bort?
data class Veileder(
    val onPremisesSamAccountName: String?,
    val givenName: String?,
    val surname: String?,
    val mail: String?,
    val businessPhones: String?,
    val accountEnabled: Boolean,
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
    val id: String,
    val displayName: String,
)
