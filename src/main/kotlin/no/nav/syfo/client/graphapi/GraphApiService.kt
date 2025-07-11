package no.nav.syfo.client.graphapi

import com.microsoft.graph.serviceclient.GraphServiceClient
import no.nav.syfo.veileder.Gruppe
import no.nav.syfo.veileder.Veileder

// TODO: Rename
interface GraphApiService {
    /**
     * @throws no.nav.syfo.application.api.exception.RestException
     */
    fun getGroupsForVeileder(graphServiceClient: GraphServiceClient): List<Gruppe>

    /**
     * @throws no.nav.syfo.application.api.exception.RestException
     */
    fun getMembersByGroupId(graphServiceClient: GraphServiceClient, groupId: String): List<Veileder>
}
