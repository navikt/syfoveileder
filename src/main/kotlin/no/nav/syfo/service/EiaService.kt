package no.nav.syfo.service

import no.nav.syfo.controller.domain.SmSykmeld
import no.nav.syfo.repository.EiaDAO
import org.springframework.stereotype.Service

@Service
class EiaService(val eiaDAO: EiaDAO) {

    fun hentRaderFraEia() : List<SmSykmeld> = eiaDAO.hentSykmeldinger().map { SmSykmeld(it.SykmeldigId, it.meldingId, it.telefon) }

}
