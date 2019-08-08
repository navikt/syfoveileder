package no.nav.syfo.util

object TestData {
    val userListResponseBody = "{\n" +
            "    \"@odata.context\": \"https://graph.microsoft.com/v1.0/\$metadata#users(onPremisesSamAccountName,givenName,surname,streetAddress,city)\",\n" +
            "    \"@odata.nextLink\": \"https://graph.microsoft.com/v1.0/users/?\$filter=city+eq+'NAV%20X-files'&\$select=onPremisesSamAccountName%2cgivenName%2csurname%2cstreetAddress%2ccity&\$skiptoken=X'44537074090001000000000000000014000000DDE2A3E7B5A9244DB391C7B5E55D1DF201000000000000000000000000000017312E322E3834302E3131333535362E312E342E3233333102000000000001C7D60D18A735D441B7703E043EA6192D'\",\n" +
            "    \"value\": [\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z777777\",\n" +
            "            \"givenName\": \"A.D \",\n" +
            "            \"surname\": \"Skinner\",\n" +
            "            \"streetAddress\": \"2990\",\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z888888\",\n" +
            "            \"givenName\": \"Fox\",\n" +
            "            \"surname\": \"Mulder\",\n" +
            "            \"streetAddress\": null,\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"onPremisesSamAccountName\": \"Z999999\",\n" +
            "            \"givenName\": \"Dana\",\n" +
            "            \"surname\": \"Scully\",\n" +
            "            \"streetAddress\": \"0123\",\n" +
            "            \"city\": \"NAV X-FILES\"\n" +
            "        }\n" +
            "    ]\n" +
            "}"

    val userListEmptyValueResponseBody = "{\n" +
            "    \"@odata.context\": \"https://graph.microsoft.com/v1.0/\$metadata#users(onPremisesSamAccountName,givenName,surname,streetAddress,city)\",\n" +
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

    val enhetNavnResponseBody = "{\n" +
            "            \"enhedId\": 100000099,\n" +
            "            \"navn\": \"NAV X-Files\",\n" +
            "            \"enhetNr\": \"0123\",\n" +
            "            \"antallRessurser\": 111,\n" +
            "            \"status\": \"Aktiv\",\n" +
            "            \"orgNivaa\": \"EN\",\n" +
            "            \"type\": \"LOKAL\",\n" +
            "            \"organisasjonsnummer\": \"987654321\",\n" +
            "            \"underEtableringsDato\": \"1970-01-01\",\n" +
            "            \"aktiveringsdato\": \"1970-01-01\",\n" +
            "            \"underAvviklingDato\": null,\n" +
            "            \"nedleggelsesdato\": null,\n" +
            "            \"oppgavebehandler\": true,\n" +
            "            \"versjon\": 42,\n" +
            "            \"sosialeTjeneste\": \"Bla bla\",\n" +
            "            \"kanalstrategi\": \"Bla bla\",\n" +
            "            \"orgNrTilKommunaltNavKontor\": \"876543219\"\n" +
            "}"

    val enhetNavnUkjentEnhetsnummer = "{\n" +
            "            \"field\": null,\n" +
            "            \"message\": \"Enheten med nummeret \'0000\' eksisterer ikke\"\n" +
            "}"
}
