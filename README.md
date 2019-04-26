# syfoprediksjoner

## Om syfoprediksjoner
syfoprediksjoner er en Spring Boot applikasjon skrevet i Kotlin. Hovedoppgaven til syfoprediksjoner
er å tilby sykmeldingsdata til applikasjoner som skal predikere utfallet av et sykefravær via REST-grensesnitt.

## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`
