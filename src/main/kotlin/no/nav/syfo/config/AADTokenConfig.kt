package no.nav.syfo.config

import com.microsoft.aad.adal4j.AuthenticationContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class AADTokenConfig {

    @Bean
    fun authenticationContext( @Value("\${aadauthority.url}") authority: String): AuthenticationContext {
        val service = Executors.newFixedThreadPool(1)
        return AuthenticationContext(authority,true, service) // Authority er url til Azure
    }
}
