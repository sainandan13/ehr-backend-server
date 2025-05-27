package sirobilt.meghasanjivini.masterdata.repository


import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import sirobilt.meghasanjivini.masterdata.dto.FacilitySuggestionDto
import sirobilt.meghasanjivini.masterdata.model.Facility
import java.util.*

@ApplicationScoped
class FacilityRepository : PanacheRepository<Facility> {
    fun suggestByName(name: String, page: Int, size: Int): List<FacilitySuggestionDto> {
        return find("lower(facilityName) LIKE ?1",
            "%${name.lowercase()}%")
            .page(page, size)
            .list()
            .map { FacilitySuggestionDto(it.hospitalId, it.facilityName) }
    }

    fun countByName(name: String): Long {
        return count("lower(facilityName) LIKE ?1", "%${name.lowercase()}%")
    }
}
