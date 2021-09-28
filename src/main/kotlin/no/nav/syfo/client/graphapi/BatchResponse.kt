package no.nav.syfo.client.graphapi

import no.nav.syfo.veileder.Veileder

data class GraphBatchRequest(
    val requests: List<RequestEntry>,
)

data class RequestEntry(
    val id: String,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
)

data class BatchResponse(
    val responses: List<BatchBody>,
)

data class BatchBody(
    val id: String,
    val body: GetUsersResponse,
)

data class GetUsersResponse(
    val value: List<AADVeileder>,
)

data class AADVeileder(
    val givenName: String,
    val surname: String,
    val mailNickname: String,
)

fun AADVeileder.toVeileder() =
    Veileder(
        fornavn = givenName,
        etternavn = surname,
        ident = mailNickname,
    )
