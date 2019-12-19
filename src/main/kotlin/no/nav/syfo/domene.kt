package no.nav.syfo

import java.time.LocalDateTime

data class Veileder(
        val ident: String,
        val fornavn: String?,
        val etternavn: String?
)

data class AADVeileder(
        val givenName: String,
        val surname: String,
        val mailNickname: String
)

fun AADVeileder.toVeileder(): Veileder =
        Veileder(fornavn = givenName, etternavn = surname, ident = mailNickname)

data class GraphBatchRequest(val requests: List<RequestEntry>)

data class RequestEntry(
        val id: String,
        val method: String,
        val url: String,
        val headers: Map<String, String>
)

data class BatchResponse(
        val responses: List<BatchBody>)

data class BatchBody(val id: String, val body: GetUsersResponse)

data class GetUsersResponse(
        val value: List<AADVeileder>
)

data class AxsysVeileder(
        val appIdent: String,
        val historiskIdent: Number
)

fun AxsysVeileder.toVeileder(): Veileder =
        Veileder(fornavn = "", etternavn = "", ident = appIdent)

data class AADToken(
        val accessToken: String,
        val refreshToken: String?,
        val expires: LocalDateTime
)
