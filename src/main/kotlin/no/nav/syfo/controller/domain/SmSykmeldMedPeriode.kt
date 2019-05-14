package no.nav.syfo.controller.domain

import java.time.LocalDate

data class SmSykmeldMedPeriode(
        var fomDato: LocalDate?,
        var tomDato: LocalDate?,
        var grad: Long?,
        var signaturDato: LocalDate?,
        var annenDato: LocalDate?,
        var pasientFodselsnummer: String?,
        var harFlereArbeidsforhold: Long?,
        var navnFastlege: String?,
        var navkontor: String?,
        var navnArbeidsgiver: String?,
        var yrkesBetegnelse: String?,
        var antallTimer: Long?,
        var stillingsProsent: String?,
        var skadeDato: LocalDate?,
        var aktivitetMuligFraDato: LocalDate?,
        var veiledningBeskrivelse: String?,
        var harTilbakemeldingArbeidsgiver: Long?,
        var harErklering: Long?,
        var harInformasjon: Long?,
        var meldingTilNav: String?,
        var friskmeldingsdato: LocalDate?,
        var erArbeidsforEtterPeriode: Long?,
        var gjelderType: String,
        var erSvangerskap: Long?,
        var erAnnenFraversgrunn: Long?,
        var erYrkessykdom: Long?
)
