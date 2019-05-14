package no.nav.syfo.repository

import no.nav.syfo.repository.domain.PSmSykmeld
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

    fun hentSykmeldinger() : List<PSmSykmeld> {
        return jdbcTemplate.query("SELECT * FROM sm_sykmeld ORDER BY sykmelding_id OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", PSmSykmeldRowMapper())
    }

    private inner class PSmSykmeldRowMapper : RowMapper<PSmSykmeld> {
        override fun mapRow(rs: ResultSet, rowNum: Int): PSmSykmeld? {
            return PSmSykmeld(
                    rs.getLong("sykmelding_id"),
                    rs.getLong("melding_id"),
                    rs.getString("telefon")
            )
        }

    }

}
