package no.nav.syfo.client.graphapi

import com.microsoft.graph.models.Group
import com.microsoft.graph.models.User
import com.microsoft.graph.serviceclient.GraphServiceClient

// TODO: Rename
interface GraphApiService {
    /**
     * @throws no.nav.syfo.application.api.exception.RestException
     */
    fun getGroupsForVeileder(graphServiceClient: GraphServiceClient): List<Group>

    /**
     * @throws no.nav.syfo.application.api.exception.RestException
     */
    fun getMembersByGroupId(graphServiceClient: GraphServiceClient, group: Group): List<User>
}
