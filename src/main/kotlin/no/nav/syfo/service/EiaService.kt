package no.nav.syfo.service

import no.nav.syfo.controller.domain.SmSykmeldMedPeriode
import no.nav.syfo.repository.EiaDAO
import org.springframework.stereotype.Service

@Service
class EiaService(val eiaDAO: EiaDAO) {

    fun hentSykmeldingerMedPeriodeFraEia() : List<SmSykmeldMedPeriode> = eiaDAO.hentSykmeldingerKombinertMedPerioder()

    fun hentSykmeldingerMedPeriodeFraEia(offset: Long, nrRows: Long) : List<SmSykmeldMedPeriode> = eiaDAO.hentSykmeldingerKombinertMedPerioder(offset, nrRows)

}
