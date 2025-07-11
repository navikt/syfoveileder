package no.nav.syfo.client.graphapi

import com.microsoft.graph.core.tasks.PageIterator
import com.microsoft.graph.models.DirectoryObjectCollectionResponse
import com.microsoft.graph.models.Group
import com.microsoft.graph.models.User
import com.microsoft.graph.serviceclient.GraphServiceClient
import no.nav.syfo.application.api.exception.toRestException
import no.nav.syfo.veileder.Gruppe
import no.nav.syfo.veileder.Veileder
import java.util.*

open class GraphApiServiceImpl : GraphApiService {
    override fun getGroupsForVeileder(graphServiceClient: GraphServiceClient): List<Gruppe> {
        try {
            return getGroupsForVeilederRequest(graphServiceClient)
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
    protected open fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
        // me/memberOf
        val directoryObjectCollectionResponse = graphServiceClient.me().memberOf().get { requestConfiguration ->
            requestConfiguration.headers.add("ConsistencyLevel", "eventual")
            requestConfiguration.queryParameters.select =
                arrayOf(
                    "id",
                    "displayName",
                    "onPremisesSamAccountName",
                    "description"
                )
            requestConfiguration.queryParameters.count = true
        }

        // https://learn.microsoft.com/en-us/graph/sdks/paging?tabs=java
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

    override fun getMembersByGroupId(graphServiceClient: GraphServiceClient, groupId: String): List<Veileder> {
        try {
            return getMembersByGroupIdRequest(graphServiceClient, groupId)
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
    protected open fun getMembersByGroupIdRequest(graphServiceClient: GraphServiceClient, groupId: String): List<User> {
        // /groups/<groupId>/members
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

    private fun Group.toGruppe(): Gruppe {
        return Gruppe(
            id = this.id,
            displayName = this.displayName,
            description = this.description,
            onPremisesSamAccountName = this.onPremisesSamAccountName,
        )
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
}
