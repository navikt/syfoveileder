package no.nav.syfo.repository

import java.sql.Date
import java.time.LocalDate

object DbUtil {

    fun tilLocalDate(date: Date?): LocalDate? {
        return date?.toLocalDate()
    }

}
