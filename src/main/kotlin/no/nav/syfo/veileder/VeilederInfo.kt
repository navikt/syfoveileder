package no.nav.syfo.veileder

data class VeilederInfo(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val epost: String,
    val telefonnummer: String? = null,
    val enabled: Boolean? = null,
)

// TODO: Erstatte med modifisert VeilederInfo? Kan også f.eks. filtrere bort de som ikke har navn og andre ting vi trenger
data class Veileder(
    val onPremisesSamAccountName: String?,
    val givenName: String?,
    val surname: String?,
    val mail: String?,
    val businessPhones: String?, // FRom list (should only ever contain one) to string
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

// TODO: Fjerne overflødige properties. Tror bare id + displayNAme er relevant.
data class Gruppe(
    val id: String, // UUID
    val displayName: String,
    val description: String?,
    val onPremisesSamAccountName: String?,
)
