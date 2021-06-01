package no.nav.syfo.util

import no.nav.syfo.AxsysVeileder
import no.nav.syfo.Veileder

object TestData {
    val userListResponseBodyGraphApi = "{\n" +
        "    \"responses\": [\n" +
        "        {\n" +
        "            \"id\": \"1\",\n" +
        "            \"status\": 200,\n" +
        "            \"headers\": {\n" +
        "                \"Cache-Control\": \"no-cache\",\n" +
        "                \"OData-Version\": \"4.0\",\n" +
        "                \"Content-Type\": \"application/json;odata.metadata=minimal;odata.streaming=true;IEEE754Compatible=false;charset=utf-8\"\n" +
        "            },\n" +
        "            \"body\": {\n" +
        "                \"@odata.context\": \"https://graph.microsoft.com/v1.0/\$metadata#users(mailNickname,givenName,surname)\",\n" +
        "                \"value\": [\n" +
        "                      {\n" +
        "            \"mailNickname\": \"Z777777\",\n" +
        "            \"givenName\": \"A.D \",\n" +
        "            \"surname\": \"Skinner\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"mailNickname\": \"Z888888\",\n" +
        "            \"givenName\": \"Fox\",\n" +
        "            \"surname\": \"Mulder\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"mailNickname\": \"Z999999\",\n" +
        "            \"givenName\": \"Dana\",\n" +
        "            \"surname\": \"Scully\"\n" +
        "        }\n" +
        "                ]\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}"

    val getAxsysVeiledereResponseBody = "[\n" +
        "    {\n" +
        "        \"appIdent\": \"Z999999\",\n" +
        "        \"historiskIdent\": 123456789\n" +
        "    },\n" +
        "    {\n" +
        "        \"appIdent\": \"Z8888888\",\n" +
        "        \"historiskIdent\": 987654321\n" +
        "    }\n" +
        "]"

    val errorResponseBodyAxsys = "{\n" +
        "    \"melding\": \"Enhet 0999 eksisterer ikke\"\n" +
        "}"

    val userListEmptyValueResponseBody = "{\n" +
        "    \"@odata.context\": \"https://graph.microsoft.com/v1.0/\$metadata#users(mailNickname,givenName,surname)\",\n" +
        "    \"value\": []\n" +
        "}"

    val errorResponseBodyGraphApi = "{\n" +
        "  \"error\": {\n" +
        "    \"code\": \"serviceNotAvailable\",\n" +
        "    \"message\": \"There was an internal server error while processing the request..\",\n" +
        "    \"innerError\": {\n" +
        "      \"requestId\": \"request-id\",\n" +
        "      \"date\": \"date-time\"\n" +
        "    }\n" +
        "  }\n" +
        "}"

    val brukereResponseBody = "[\n" +
        "    {\n" +
        "        \"appIdent\": \"Z999999\",\n" +
        "        \"historiskIdent\": 123456789\n" +
        "    },\n" +
        "    {\n" +
        "        \"appIdent\": \"Z666666\",\n" +
        "        \"historiskIdent\": 111111111\n" +
        "    }" +
        "]"

    val AxsysVeiledere = listOf(
        AxsysVeileder(appIdent = "Z999999", historiskIdent = 123),
        AxsysVeileder(appIdent = "Z888888", historiskIdent = 123)
    )

    val AADVeiledere = listOf<Veileder>(
        Veileder(ident = "Z999999", fornavn = "Dana", etternavn = "Scully"),
        Veileder(ident = "Z888888", fornavn = "", etternavn = "")
    )
}
