package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.testhelper.mock.getMockHttpClient
import no.nav.syfo.testhelper.mock.wellKnownInternalAzureAD

class ExternalMockEnvironment() {
    val applicationState: ApplicationState = testAppState()
    val environment = testEnvironment()
    val mockHttpClient = getMockHttpClient(env = environment)
    val valkeyCache = InMemoryValkeyStore()
    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}
