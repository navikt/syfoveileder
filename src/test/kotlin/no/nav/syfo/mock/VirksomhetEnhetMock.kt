package no.nav.syfo.mock

import no.nav.virksomhet.organisering.enhetogressurs.v1.Ressurs
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.*
import no.nav.virksomhet.tjenester.enhet.v1.Enhet
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = "mockVirksomhetEnhet_V1", havingValue = "true")
class VirksomhetEnhetMock : Enhet {
    override fun hentRessursListe(p0: WSHentRessursListeRequest?): WSHentRessursListeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun finnEnhetListe(p0: WSFinnEnhetListeRequest?): WSFinnEnhetListeResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ping() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hentEnhetListe(p0: WSHentEnhetListeRequest?): WSHentEnhetListeResponse {
        return WSHentEnhetListeResponse()
                .withRessurs(Ressurs()
                        .withFornavn("Dana")
                        .withEtternavn("Scully")
                        .withNavn("Katherine")
                        .withRessursId("Z999999"))
    }
}
