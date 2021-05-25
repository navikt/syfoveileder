package no.nav.syfo.veilederinfo.v2

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.getSubjectInternAzureV2
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.util.getOrCreateCallId
import no.nav.syfo.veilederinfo.VeilederInfoDTO
import no.nav.syfo.veilederinfo.toVeilederDTO
import no.nav.syfo.veilederinfo.v2.VeilederInfoControllerV2.Companion.API_VEILEDER_BASE_PATH
import no.nav.syfo.veiledernavn.VeilederService
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
@RestController
@RequestMapping(value = [API_VEILEDER_BASE_PATH])
class VeilederInfoControllerV2 @Inject constructor(
    private val metric: Metric,
    private val contextHolder: TokenValidationContextHolder,
    private val veilederService: VeilederService
) {
    @GetMapping(
        value = [API_VEILEDER_SELF_PATH],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getVeilederInfo(
        @RequestHeader headers: MultiValueMap<String, String>,
    ): VeilederInfoDTO {
        val callId = getOrCreateCallId(headers)

        val veilederIdent: String = getSubjectInternAzureV2(contextHolder)
        metric.countIncomingRequests("veileder_self")
        return veilederService.veilederInfo(
            callId = callId,
            veilederIdent = veilederIdent
        ).toVeilederDTO()
    }

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
        const val API_VEILEDER_BASE_PATH = "/api/v2/veileder"
        const val API_VEILEDER_SELF_PATH = "/self"
    }
}
