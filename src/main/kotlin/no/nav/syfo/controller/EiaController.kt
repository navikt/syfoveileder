package no.nav.syfo.controller

import no.nav.security.spring.oidc.validation.api.Unprotected
import no.nav.syfo.controller.domain.SmSykmeldMedPeriode
import no.nav.syfo.service.EiaService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
@RequestMapping("/api/eia/")
@Unprotected
class EiaController @Inject constructor(val eiaService: EiaService){

    @ResponseBody
    @GetMapping(value = ["/sykmeldingMedPeriode"], produces = [APPLICATION_JSON_VALUE])
    fun hentEiaSykmeldingerMedPeriode() : List<SmSykmeldMedPeriode> {
        return eiaService.hentSykmeldingerMedPeriodeFraEia()
    }

}
