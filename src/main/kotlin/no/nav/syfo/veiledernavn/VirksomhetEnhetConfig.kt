import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.STSClientConfig
import no.nav.syfo.consumer.util.ws.WsClient
import no.nav.virksomhet.tjenester.enhet.v1.Enhet
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.*

@Configuration
class VirksomhetEnhetConfig {
    // TODO: Is it possible to have config files in a different folder than ApplicationConfig, like this?
    // TODO: add credentials in local test file
    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockVirksomhetEnhet_V1", havingValue = "false", matchIfMissing = true)
    @Primary
    fun virksomhetEnhet(@Value("\${virksomhet.enhet.v1.endpointurl}") serviceUrl: String) : Enhet {
        val port: Enhet = WsClient<Enhet>().createPort(serviceUrl, Enhet::class.java, Collections.singletonList(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

}
