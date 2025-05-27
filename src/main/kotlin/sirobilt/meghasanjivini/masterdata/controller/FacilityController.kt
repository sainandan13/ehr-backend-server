package sirobilt.meghasanjivini.masterdata.controller

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import sirobilt.meghasanjivini.masterdata.service.FacilityService

@Path("/facilities")
@Produces(MediaType.APPLICATION_JSON)
class FacilityResource @Inject constructor(
    private val facilityService: FacilityService
) {

    @GET
    @Path("/suggest")
    fun suggestFacilities(
        @QueryParam("name") name: String?,
        @QueryParam("page") page: Int?,
        @QueryParam("size") size: Int?
    ): Response {
        if (name.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Query param 'name' is required")).build()
        }
        val pageNum = page ?: 0
        val pageSize = size ?: 10
        val (facilities, total) = facilityService.getFacilitySuggestions(name, pageNum, pageSize)
        return Response.ok(
            mapOf(
                "results" to facilities,
                "total" to total,
                "page" to pageNum,
                "size" to pageSize
            )
        ).build()
    }
}