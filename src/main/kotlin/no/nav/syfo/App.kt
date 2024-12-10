package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.axsys.AxsysClient
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.graphapi.GraphApiClient
import no.nav.syfo.client.wellknown.getWellKnown
import no.nav.syfo.veiledernavn.VeilederService
import org.slf4j.LoggerFactory
import redis.clients.jedis.*
import java.util.concurrent.TimeUnit

const val applicationPort = 8080

fun main() {
    val applicationState = ApplicationState()
    val environment = Environment()

    val redisConfig = environment.redisConfig
    val cache = RedisStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(redisConfig.host, redisConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(redisConfig.ssl)
                .user(redisConfig.redisUsername)
                .password(redisConfig.redisPassword)
                .database(redisConfig.redisDB)
                .build()
        )
    )
    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        graphApiUrl = environment.graphapiUrl,
        cache = cache,
    )
    val axsysClient = AxsysClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.axsysUrl,
        clientId = environment.axsysClientId,
        cache = cache,
    )
    val graphApiClient = GraphApiClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.graphapiUrl,
        cache = cache,
    )

    val veilederService = VeilederService(
        axsysClient = axsysClient,
        graphApiClient = graphApiClient,
    )

    val applicationEngineEnvironment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        config = HoconApplicationConfig(ConfigFactory.load())

        connector {
            port = applicationPort
        }

        val wellKnownInternalAzureAD = getWellKnown(
            wellKnownUrl = environment.azureAppWellKnownUrl
        )

        module {
            apiModule(
                applicationState = applicationState,
                environment = environment,
                wellKnownInternalAzureAD = wellKnownInternalAzureAD,
                veilederService = veilederService,
            )
        }
    }

    applicationEngineEnvironment.monitor.subscribe(ApplicationStarted) { application ->
        applicationState.ready = true
        application.environment.log.info("Application is ready, running Java VM ${Runtime.version()}")
    }

    val server = embeddedServer(
        factory = Netty,
        environment = applicationEngineEnvironment,
    ) {
        connectionGroupSize = 8
        workerGroupSize = 8
        callGroupSize = 16
    }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )

    server.start(wait = true)
}
