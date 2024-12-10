package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.client.axsys.AxsysVeileder
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_2

fun generateAxsysResponse() = listOf(
    AxsysVeileder(
        appIdent = VEILEDER_IDENT,
        historiskIdent = 123456789,
    ),
    AxsysVeileder(
        appIdent = VEILEDER_IDENT_2,
        historiskIdent = 987654321,
    ),
)

fun MockRequestHandleScope.axsysMockResponse(): HttpResponseData = respond(generateAxsysResponse())
