package sirobilt.meghasanjivini.masterdata.controller

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import sirobilt.meghasanjivini.masterdata.dto.*
import sirobilt.meghasanjivini.masterdata.model.LookupValue
import sirobilt.meghasanjivini.masterdata.service.*
import java.util.UUID

@Path("/lookup-values")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LookupValueController(
    private val service: LookupService
) {

    @GET
    @Path("/{category}")
    fun getByCategory(@PathParam("category") category: String): List<LookupDto> {
        return service.getByCategory(category)
    }

    @GET
    fun getAll(): List<LookupValue> = service.getAll()

    @POST
    fun create(dto: LookupValueCreateDTO): Response {
        val entity = service.create(dto)
        return Response.status(Response.Status.CREATED).entity(entity).build()
    }

    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: UUID, dto: LookupValueUpdateDTO): LookupValue {
        return service.update(id, dto)
    }

    @DELETE
    @Path("/{category}/{value}")
    fun deactivate(
        @PathParam("category") category: String,
        @PathParam("value") value: String
    ): LookupValue {
        return service.deactivate(category, value)
    }
}
@Path("/geo")
@Produces(MediaType.APPLICATION_JSON)
class GeographyResource(
    private val countryService: CountryService,
    private val stateService: StateService,
    private val districtService: DistrictService
) {
    @GET @Path("/countries")
    fun countries(): List<CountryDto> = countryService.list()

    @GET @Path("/countries/{id}/states")
    fun states(@PathParam("id") countryId: UUID): List<StateDto> =
        stateService.listByCountry(countryId)

    @GET @Path("/states/{id}/districts")
    fun districts(@PathParam("id") stateId: UUID): List<DistrictDto> =
        districtService.listByState(stateId)
}
