package no.nav.syfo.repository

import no.nav.syfo.controller.domain.SmSykmeldMedPeriode
import no.nav.syfo.repository.DbUtil.tilLocalDate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet

@Service
@Repository
@Transactional
class EiaDAO(private val jdbcTemplate: JdbcTemplate) {


    fun hentSykmeldingerKombinertMedPerioder() : List<SmSykmeldMedPeriode> {
        return jdbcTemplate.query("select sp.fom_dato, sp.tom_dato, sp.grad, sm.signaturdato, sm.annendato, sm.pasientfodselsnummer, sm.harflerearbeidsforhold, navnfastlege, navkontor, navnarbeidsgiver, yrkesbetegnelse, antalltimer, stillingsprosent, skadedato, sm.AKTIVITETMULIGFRADATO, sm.VEILEDNINGBESKRIVELSE, hartilbakemeldingarbeidsgiver, harerklering, harinformasjon, meldingtilnav, friskmeldingsdato, erarbeidsforetterperiode, gjelder_type, ersvangerskap, erannenfraversgrunn, eryrkessykdom from SM_SYKMELD sm, SM_PERIODE sp where sm.sykmelding_id = sp.sykmelding_id", SmSykmeldOgPeriodeRowMapper())
    }

    private inner class SmSykmeldOgPeriodeRowMapper : RowMapper<SmSykmeldMedPeriode> {
        override fun mapRow(rs: ResultSet, rowNum: Int): SmSykmeldMedPeriode? {
            return SmSykmeldMedPeriode(
                    tilLocalDate(rs.getDate("fom_dato")),
                    tilLocalDate(rs.getDate("tom_dato")),
                    rs.getLong("grad"),
                    tilLocalDate(rs.getDate("signaturdato")),
                    tilLocalDate(rs.getDate("annendato")),
                    rs.getString("pasientfodselsnummer"),
                    rs.getLong("harflerearbeidsforhold"),
                    rs.getString("navnfastlege"),
                    rs.getString("navkontor"),
                    rs.getString("navnarbeidsgiver"),
                    rs.getString("yrkesbetegnelse"),
                    rs.getLong("antalltimer"),
                    rs.getString("stillingsprosent"),
                    tilLocalDate(rs.getDate("skadedato")),
                    tilLocalDate(rs.getDate("aktivitetmuligfradato")),
                    rs.getString("veiledningbeskrivelse"),
                    rs.getLong("hartilbakemeldingarbeidsgiver"),
                    rs.getLong("harerklering"),
                    rs.getLong("harinformasjon"),
                    rs.getString("meldingtilnav"),
                    tilLocalDate(rs.getDate("friskmeldingsdato")),
                    rs.getLong("erarbeidsforetterperiode"),
                    rs.getString("gjelder_type"),
                    rs.getLong("ersvangerskap"),
                    rs.getLong("erannenfraversgrunn"),
                    rs.getLong("eryrkessykdom")
            )
        }

    }

}
