package no.nav.syfo.testhelper.mock

import com.microsoft.graph.models.Group
import com.microsoft.graph.models.User
import com.microsoft.graph.serviceclient.GraphServiceClient
import no.nav.syfo.client.graphapi.GraphApiServiceImpl

open class GraphApiServiceMock : GraphApiServiceImpl() {
    override fun getGroupsForVeilederRequest(graphServiceClient: GraphServiceClient): List<Group> {
        return listOf(group())
    }

    override fun getMembersByGroupIdRequest(graphServiceClient: GraphServiceClient, group: Group): List<User> {
        return listOf(user(), userWithNullFields())
    }
}
