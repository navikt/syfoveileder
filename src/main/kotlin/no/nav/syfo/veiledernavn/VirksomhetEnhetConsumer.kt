package no.nav.syfo.veiledernavn

import no.nav.syfo.Veileder
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeRequest
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeResponse
import no.nav.virksomhet.tjenester.enhet.v1.Enhet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class VirksomhetEnhetConsumer {

    @Inject
    private val virksomhetEnhet: Enhet? = null


    @Throws(Exception::class)
    fun hentVeilederInfo(ident: String): Veileder {
        val request = WSHentEnhetListeRequest()
        request.ressursId = ident
        try {
            val response = virksomhetEnhet!!.hentEnhetListe(request)
            return wsEnhetResponseTilVeileder(response)
        } catch (e: Exception) {
            LOG.error("Kunne ikke hente enhetene til veileder {} fra VirksomhetEnhet/NORG2", ident, e)
            throw e
        }
    }

    fun wsEnhetResponseTilVeileder(response: WSHentEnhetListeResponse): Veileder {
        val ressurs = response.ressurs
        return Veileder(ressurs.ressursId, ressurs.navn, ressurs.fornavn, ressurs.etternavn)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VirksomhetEnhetConsumer::class.java.name)
    }
}

