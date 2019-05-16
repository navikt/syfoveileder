package no.nav.syfo

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = ["org.springframework"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

inline fun <reified T> T.log(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
