package no.nav.syfo.veilederinfo

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.getSubjectInternAzure
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.OIDCIssuer.AZURE
import no.nav.syfo.util.getOrCreateCallId
import no.nav.syfo.veilederinfo.VeilederInfoController.Companion.API_VEILEDER_BASE_PATH
import no.nav.syfo.veiledernavn.VeilederService
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = [API_VEILEDER_BASE_PATH])
class VeilederInfoController @Inject constructor(
    private val metric: Metric,
    private val contextHolder: TokenValidationContextHolder,
    private val veilederService: VeilederService
) {
    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(
        value = [API_VEILEDER_SELF_PATH],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getVeilederInfo(
        @RequestHeader headers: MultiValueMap<String, String>,
    ): VeilederInfoDTO {
        val callId = getOrCreateCallId(headers)

        val veilederIdent: String = getSubjectInternAzure(contextHolder)
        metric.countIncomingRequests("veileder_self")
        return veilederService.veilederInfo(
            callId = callId,
            veilederIdent = veilederIdent
        ).toVeilederDTO()
    }

    @ProtectedWithClaims(issuer = AZURE)
    @GetMapping(
        value = ["/{navident}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getVeilederInfo(
        @PathVariable navident: String,
        @RequestHeader headers: MultiValueMap<String, String>,
    ): VeilederInfoDTO {
        val callId = getOrCreateCallId(headers)

        metric.countIncomingRequests("veileder_navident")
        return veilederService.veilederInfo(
            callId = callId,
            veilederIdent = navident
        ).toVeilederDTO()
    }

    companion object {
        const val API_VEILEDER_BASE_PATH = "/api/v1/veileder"
        const val API_VEILEDER_SELF_PATH = "/self"
    }
}
