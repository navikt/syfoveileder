# Syfoveileder

## About Syfoveileder
Syfoveileder is a Ktor application written in Kotlin. Its main job is to be a microservice offering data relating
to veiledere. So far this means a list of veileder names with access to an enhet.


## Dependencies
Syfoveileder henter ut aktive veiledere på oppgitt enhet fra Azura AD / Entra ID via Microsoft Graph.

## Technologies used

* Gradle
* Kotlin
* Ktor

#### Test Libraries:

* Mockk
* JUnit

### Requirements

* JDK 21

### Lint (Ktlint)
##### Command line
Run checking: `./gradlew --continue ktlintCheck`

Run formatting: `./gradlew ktlintFormat`
##### Git Hooks
Apply checking: `./gradlew addKtlintCheckGitPreCommitHook`

Apply formatting: `./gradlew addKtlintFormatGitPreCommitHook`

## Contact

### For NAV employees

We are available at the Slack channel `#isyfo`.
