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

                //TODO: Mulig 1.0 skal bort her
                requestUrl.startsWith("/${env.graphapiUrl}/v1.0/groups") -> graphApiGrupperMockResponse(request)

                requestUrl.startsWith("/${env.graphapiUrl}/v1.0/me/memberOf") -> graphApiGrupperMockResponse(request)

                requestUrl.startsWith("/${env.graphapiUrl}") -> graphApiMockResponse(request)

                requestUrl.startsWith("/${env.axsysUrl}") -> axsysMockResponse()

                else -> {
                    error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
}
