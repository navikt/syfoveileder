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

    val errorResponseBody = "{\n" +
            "  \"error\": {\n" +
            "    \"code\": \"serviceNotAvailable\",\n" +
            "    \"message\": \"There was an internal server error while processing the request..\",\n" +
            "    \"innerError\": {\n" +
            "      \"requestId\": \"request-id\",\n" +
            "      \"date\": \"date-time\"\n" +
            "    }\n" +
            "  }\n" +
            "}"
}


