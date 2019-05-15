package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.Unprotected
import no.nav.syfo.controller.domain.SmSykmeldMedPeriode
import no.nav.syfo.service.EiaService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.inject.Inject


@RestController
@RequestMapping("/api/eia/")
@Unprotected
class EiaController @Inject constructor(val eiaService: EiaService){


    @ResponseBody
    @GetMapping(value = ["/sykmeldingerMedPerioder"], produces = [APPLICATION_JSON_VALUE])
    fun hentEiaSykmeldingerMedPeriode(@RequestParam(required = true) offset: Long?, @RequestParam(required = true) rows: Long?) : List<SmSykmeldMedPeriode> {
        if (offset == null || rows == null)
            return eiaService.hentSykmeldingerMedPeriodeFraEia()
        return eiaService.hentSykmeldingerMedPeriodeFraEia(offset, rows)

    }

}
