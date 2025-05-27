package sirobilt.meghasanjivini.masterdata.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import sirobilt.meghasanjivini.masterdata.dto.FacilitySuggestionDto
import sirobilt.meghasanjivini.masterdata.repository.FacilityRepository

@ApplicationScoped
class FacilityService @Inject constructor(
    private val facilityRepository: FacilityRepository
) {

    fun getFacilitySuggestions(name: String, page: Int, size: Int): Pair<List<FacilitySuggestionDto>, Long> {
        val results = facilityRepository.suggestByName(name, page, size)
        val total = facilityRepository.countByName(name)
        return results to total
    }
}
