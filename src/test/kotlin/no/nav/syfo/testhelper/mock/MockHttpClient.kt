package no.nav.syfo.testhelper.mock

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import no.nav.syfo.application.Environment
import no.nav.syfo.util.configure

fun getMockHttpClient(env: Environment) = HttpClient(MockEngine) {
    install(ContentNegotiation) {
        jackson { configure() }
    }
    engine {
        addHandler { request ->
            val requestUrl = request.url.encodedPath
            when {
                requestUrl == "/${env.azureOpenidConfigTokenEndpoint}" -> azureAdMockResponse()

                requestUrl.startsWith("/${env.graphapiUrl}") -> graphApiMockResponse(request)

                else -> {
                    error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
}
