# syfoveileder

## Om syfoveileder
syfoveileder er en Spring Boot applikasjon skrevet i Kotlin. Hovedoppgaven til syfoveileder
er å være en mikrotjeneste som tilbyr data med knytning til veiledere. Feks veileder navn og tildelte brukere. 

## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`
