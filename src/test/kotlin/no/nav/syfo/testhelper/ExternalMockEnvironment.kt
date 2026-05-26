package no.nav.syfo.testhelper

import io.mockk.mockk
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.testhelper.mock.getMockHttpClient
import no.nav.syfo.testhelper.mock.wellKnownInternalAzureAD
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import kotlin.test.assertNotNull

class ExternalMockEnvironment() {
    val applicationState: ApplicationState = testAppState()
    val environment = testEnvironment()
    val mockHttpClient = getMockHttpClient(env = environment)
    val valkeyCache = mockk<ValkeyStore>(relaxed = true)
    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()
}
