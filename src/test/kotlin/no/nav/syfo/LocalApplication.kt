package no.nav.syfo

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = ["org.springframework"])
class LocalApplication

fun main(args: Array<String>) {
    runApplication<LocalApplication>(*args)
}
