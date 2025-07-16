package no.nav.syfo.client.graphapi

import com.microsoft.graph.core.tasks.PageIterator
import com.microsoft.graph.models.DirectoryObjectCollectionResponse
import com.microsoft.graph.models.Group
import com.microsoft.graph.models.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.application.api.exception.toRestException
import no.nav.syfo.application.cache.ValkeyStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientProxy
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.callIdArgument
import no.nav.syfo.veileder.Gruppe
import no.nav.syfo.veileder.Veileder
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import java.util.Objects

class GraphApiClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val cache: ValkeyStore,
    private val httpClient: HttpClient = httpClientProxy(),
) {

    suspend fun veileder(
        callId: String,
        veilederIdent: String,
        token: String,
    ): GraphApiUser? {
        val cachedObject: GraphApiUser? = cache.getObject(cacheKey(veilederIdent))
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT.increment()
            cachedObject
        } else {
            val oboToken = azureAdClient.getOnBehalfOfToken(
                scopeClientId = baseUrl,
                token = token,
            )?.accessToken
                ?: throw RuntimeException("Failed to request access to Veileder in Graph API: Failed to get OBO token")

            getVeilederGraphApiUser(veilederIdent = veilederIdent, token = oboToken, callId = callId)
        }
    }

    suspend fun veilederMedSystemToken(
        callId: String,
        token: String,
        veilederIdent: String,
    ): GraphApiUser? {
        val cachedObject: GraphApiUser? = cache.getObject(cacheKey(veilederIdent))
        return if (cachedObject != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_HIT.increment()
            cachedObject
        } else {
            val systemToken = azureAdClient.getSystemToken(
                token = token,
                scopeClientId = baseUrl,
            )?.accessToken
                ?: throw RuntimeException("Failed to request access to Veileder in Graph API: Failed to get system token")

            getVeilederGraphApiUser(veilederIdent = veilederIdent, token = systemToken, callId = callId)
        }
    }

    private suspend fun getVeilederGraphApiUser(
        veilederIdent: String,
        token: String,
        callId: String
    ) = try {
        val queryFilter = "startsWith(onPremisesSamAccountName, '$veilederIdent')"
        val queryFilterWhitespaceEncoded = queryFilter.replace(" ", "%20")
        val url =
            "$baseUrl/v1.0/users?\$filter=$queryFilterWhitespaceEncoded&\$select=onPremisesSamAccountName,givenName,surname,mail,businessPhones,accountEnabled&\$count=true"

        val response: GraphApiGetUserResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, bearerHeader(token))
            header("ConsistencyLevel", "eventual")
            accept(ContentType.Application.Json)
        }.body()
        COUNT_CALL_GRAPHAPI_VEILEDER_SUCCESS.increment()
        val graphAPIUser = response.value.firstOrNull()
        if (graphAPIUser != null) {
            cache.setObject(
                expireSeconds = CACHE_EXPIRATION_SECONDS,
                key = cacheKey(veilederIdent),
                value = graphAPIUser,
            )
        }
        COUNT_CALL_GRAPHAPI_VEILEDER_CACHE_MISS.increment()
        graphAPIUser
    } catch (e: ResponseException) {
        COUNT_CALL_GRAPHAPI_VEILEDER_FAIL.increment()
        log.error(
            "Error while requesting Veileder from GraphApi {}, {}, {}",
            StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
            StructuredArguments.keyValue("message", e.message),
            callIdArgument(callId),
        )
        throw e
    }

    suspend fun getVeiledereByEnhetNr(callId: String, token: String, enhetNr: String): List<Veileder> {
        return getEnhetByEnhetNrForVeileder(
            token = token,
            enhetNr = enhetNr,
        )?.let { group ->
            getVeiledereVedEnhetByGroupId(
                token = token,
                group = group,
            )
        } ?: run {
            log.warn("User has no groups or there are no veiledere in specified group. CallId=$callId")
            emptyList()
        }
    }

    suspend fun getEnhetByEnhetNrForVeileder(token: String, enhetNr: String): Gruppe? {
        val veilederIdent = getNAVIdentFromToken(token)
        val key = cacheKeyGrupper(veilederIdent)
        val cachedGroups: List<Gruppe>? = cache.getListObject(key)

        val grupper = if (cachedGroups != null) {
            COUNT_CALL_GRAPHAPI_GRUPPE_CACHE_HIT.increment()
            val harTilgang = getGruppeIfAccess(cachedGroups, enhetNr) != null

            if (harTilgang) {
                cachedGroups
            } else {
                getGroupsForVeileder(token)
            }
        } else {
            COUNT_CALL_GRAPHAPI_GRUPPE_CACHE_MISS.increment()
            getGroupsForVeileder(token)
        }

        return getGruppeIfAccess(grupper, enhetNr)?.also {
            cacheFor12Hours(key, grupper)
        }
    }

    private fun getGruppeIfAccess(grupper: List<Gruppe>, enhetNr: String): Gruppe? =
        grupper.find { it.displayName == gruppenavnEnhet(enhetNr) }

    suspend fun getGroupsForVeileder(token: String): List<Gruppe> {
        try {
            return getGroupsForVeilederRequest(token)
                .map { it.toGruppe() }
                .apply { COUNT_CALL_GRAPHAPI_GRUPPE_SUCCESS.increment() }
        } catch (e: Exception) {
            COUNT_CALL_GRAPHAPI_GRUPPE_FAIL.increment()
            throw e.toRestException("Error while getting groups for veileder")
        }
    }

    /**
     * @throws com.microsoft.kiota.ApiException
     * @throws Exception
     */
    @VisibleForTesting
    internal suspend fun getGroupsForVeilederRequest(token: String): List<Group> {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = baseUrl,
            token = token,
        )
            ?: throw RuntimeException("Failed to request list of groups for veileder in Graph API: Failed to get system token from AzureAD")

        val graphServiceClient = azureAdClient.createGraphServiceClient(azureAdToken = oboToken)
        val directoryObjectCollectionResponse = graphServiceClient.me().memberOf().get { requestConfiguration ->
            requestConfiguration.headers.add("ConsistencyLevel", "eventual")
            requestConfiguration.queryParameters.select =
                arrayOf(
                    "id",
                    "displayName",
                )
            requestConfiguration.queryParameters.count = true
        }

        val groups = mutableListOf<Group>()
        PageIterator.Builder<Group, DirectoryObjectCollectionResponse>()
            .client(graphServiceClient)
            .collectionPage(Objects.requireNonNull(directoryObjectCollectionResponse))
            .collectionPageFactory(DirectoryObjectCollectionResponse::createFromDiscriminatorValue)
            .processPageItemCallback { group -> groups.add(group) }
            .build()
            .iterate()

        return groups
    }

    private fun Group.toGruppe(): Gruppe {
        return Gruppe(
            id = this.id,
            displayName = this.displayName,
        )
    }

    suspend fun getVeiledereVedEnhetByGroupId(token: String, group: Gruppe): List<Veileder> {
        val key = cacheKeyVeiledereIEnhet(group.id)
        val cachedUsers: List<Veileder>? = cache.getListObject(key)

        return if (cachedUsers != null) {
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_HIT.increment()
            cachedUsers
        } else {
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_CACHE_MISS.increment()
            getMembersByGroupId(token, group.id).also { cacheFor12Hours(key, it) }
        }
    }

    suspend fun getMembersByGroupId(token: String, groupId: String): List<Veileder> {
        try {
            return getMembersByGroupIdRequest(token, groupId)
                .map { it.toVeileder() }
                .apply { COUNT_CALL_GRAPHAPI_VEILEDER_LIST_SUCCESS.increment() }
        } catch (e: Exception) {
            COUNT_CALL_GRAPHAPI_VEILEDER_LIST_FAIL.increment()
            throw e.toRestException("Error while getting veiledere by group id")
        }
    }

    /**
     * @throws com.microsoft.kiota.ApiException
     * @throws Exception
     */
    @VisibleForTesting
    internal suspend fun getMembersByGroupIdRequest(token: String, groupId: String): List<User> {
        val systemToken = azureAdClient.getSystemToken(
            token = token,
            scopeClientId = baseUrl,
        ) ?: throw RuntimeException("Failed to request access to Veileder in Graph API: Failed to get system token")

        val graphServiceClient = azureAdClient.createGraphServiceClient(azureAdToken = systemToken)
        val directoryObjectCollectionResponse =
            graphServiceClient.groups().byGroupId(groupId).members().get { requestConfiguration ->
                requestConfiguration.headers.add("ConsistencyLevel", "eventual")
                requestConfiguration.queryParameters.select =
                    arrayOf(
                        "givenName",
                        "surname",
                        "mail",
                        "businessPhones",
                        "onPremisesSamAccountName",
                        "accountEnabled"
                    )
                requestConfiguration.queryParameters.filter = "accountEnabled eq true"
                requestConfiguration.queryParameters.count = true
            }

        val users = mutableListOf<User>()
        PageIterator.Builder<User, DirectoryObjectCollectionResponse>()
            .client(graphServiceClient)
            .collectionPage(Objects.requireNonNull(directoryObjectCollectionResponse))
            .collectionPageFactory(DirectoryObjectCollectionResponse::createFromDiscriminatorValue)
            .processPageItemCallback { user -> users.add(user) }
            .build()
            .iterate()

        return users
    }

    private fun User.toVeileder(): Veileder {
        return Veileder(
            givenName = this.givenName,
            surname = this.surname,
            mail = this.mail,
            businessPhones = this.businessPhones?.firstOrNull(),
            accountEnabled = this.accountEnabled,
            onPremisesSamAccountName = this.onPremisesSamAccountName,
        )
    }

    private fun <T> cacheFor12Hours(cacheKey: String, value: T) {
        cache.setObject(
            key = cacheKey,
            value = value,
//            expireSeconds = CACHE_EXPIRATION_SECONDS,
            expireSeconds = 60 * 5, // TODO: For testing
        )
    }

    companion object {
        const val GRAPH_API_CACHE_VEILEDER_PREFIX = "graphapiVeileder-"
        const val GRAPH_API_CACHE_VEILEDER_GRUPPER_PREFIX = "graphapiVeilederGrupper-"
        const val GRAPH_API_CACHE_VEILEDERE_I_ENHET_PREFIX = "graphapiVeiledereIEnhet-"

        const val ENHETSNAVN_PREFIX = "0000-GA-ENHET_"

        private const val CACHE_EXPIRATION_SECONDS = (60 * 60 * 12).toLong()
        private val log = LoggerFactory.getLogger(GraphApiClient::class.java)

        private fun cacheKey(veilederIdent: String) = "$GRAPH_API_CACHE_VEILEDER_PREFIX$veilederIdent"
        fun cacheKeyGrupper(veilederIdent: String) = "$GRAPH_API_CACHE_VEILEDER_GRUPPER_PREFIX$veilederIdent"
        fun cacheKeyVeiledereIEnhet(groupId: String) = "$GRAPH_API_CACHE_VEILEDERE_I_ENHET_PREFIX$groupId"

        fun gruppenavnEnhet(enhetNr: String) = "$ENHETSNAVN_PREFIX$enhetNr"
    }
}
