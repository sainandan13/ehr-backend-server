package sirobilt.meghasanjivini.patientregistration.repository


import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Page
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import sirobilt.meghasanjivini.patientregistration.model.*
import java.time.LocalDate

@ApplicationScoped
class PatientRepository : PanacheRepositoryBase<Patient, String> {

    @Transactional
    fun softDeleteById(upId: String): Boolean {
        // This is a JPQL/Panache query; use property name
        return update("softDeleted = true WHERE upId = ?1", upId) > 0
    }



    fun findLastMrnForFacility(facilityId: String): String? {
        val paddedFacilityId = facilityId.padStart(3, '0')
        return find(
            """
            SELECT p.upId FROM Patient p 
            WHERE p.upId LIKE ?1 
            ORDER BY CAST(REPLACE(SUBSTRING(p.upId, 8), '-', '') AS long) DESC
            """.trimIndent(),
            "$paddedFacilityId-00-%"
        ).firstResult() as String?
    }

    fun search(
        upId: String?, fn: String?, ln: String?,
        mobile: String?, mail: String?,
        from: LocalDate?, to: LocalDate?
    ) = find(
        """
        SELECT DISTINCT p
          FROM Patient p
          LEFT JOIN PatientContact c ON c.patient = p
         WHERE p.softDeleted = false
           AND (:upId   IS NULL OR p.upId = :upId)
           AND (:fn   IS NULL OR p.firstName ILIKE :fn)
           AND (:ln   IS NULL OR p.lastName  ILIKE :ln)
           AND (:mob  IS NULL OR c.phoneNumber LIKE :mob)
           AND (:mail IS NULL OR c.email      ILIKE :mail)
           AND (:from IS NULL OR p.dateOfBirth >= :from)
           AND (:to   IS NULL OR p.dateOfBirth <= :to)
        """.trimIndent(),
        mapOf(
            "upId"   to upId,
            "fn"   to fn   ?.let { "%$it%" },
            "ln"   to ln   ?.let { "%$it%" },
            "mob"  to mobile?.let { "%$it%" },
            "mail" to mail ?.let { "%$it%" },
            "from" to from,
            "to"   to to
        )
    ).list()

    fun findByUpId(upId: String): Patient? {
        return find("upId = ?1 AND softDeleted = false", upId).firstResult()
    }

    fun searchByQuery(query: String, page: Int, size: Int): List<Patient> {
        val search = "%${query.lowercase()}%"
        return find(
            """
            SELECT DISTINCT p
              FROM Patient p
              LEFT JOIN PatientContact c ON c.patient = p
              LEFT JOIN PatientAddress a ON a.patient = p
             WHERE p.softDeleted = false
               AND (
                   LOWER(p.firstName) LIKE :search
                OR LOWER(p.middleName) LIKE :search
                OR LOWER(p.lastName)  LIKE :search
                OR LOWER(a.cityOrVillage) LIKE :search
                OR LOWER(c.email)     LIKE :search
                OR LOWER(c.phoneNumber) LIKE :search
               )
            """.trimIndent(),
            mapOf("search" to search)
        )
            .page(Page.of(page, size))
            .list()
    }

    fun countByQuery(query: String): Long {
        val search = "%${query.lowercase()}%"
        return find(
            """
            SELECT COUNT(DISTINCT p)
              FROM Patient p
              LEFT JOIN PatientContact c ON c.patient = p
              LEFT JOIN PatientAddress a ON a.patient = p
             WHERE p.softDeleted = false
               AND (
                   LOWER(p.firstName) LIKE :search
                OR LOWER(p.middleName) LIKE :search
                OR LOWER(p.lastName)  LIKE :search
                OR LOWER(a.cityOrVillage) LIKE :search
                OR LOWER(c.email)     LIKE :search
                OR LOWER(c.phoneNumber) LIKE :search
               )
            """.trimIndent(),
            mapOf("search" to search)
        ).firstResult() as Long
    }

    fun searchByCityOrName(city: String?, name: String?) = find(
        """
        SELECT DISTINCT p
          FROM Patient p
          JOIN PatientAddress a ON a.patient = p
         WHERE p.softDeleted = false
           AND (:city IS NULL OR LOWER(a.cityOrVillage) LIKE :city)
           AND (:name IS NULL OR LOWER(p.firstName)      LIKE :name)
        """.trimIndent(),
        mapOf(
            "city" to city?.lowercase()?.let { "%$it%" },
            "name" to name?.lowercase()?.let { "%$it%" }
        )
    ).list()

    fun findDuplicate(
        first: String,
        abhaNumber: String?,
        email: String?
    ): Patient? = find(
        """
            SELECT DISTINCT p
              FROM Patient p
              LEFT JOIN PatientContact c ON c.patient = p
             WHERE p.softDeleted = false
               AND lower(p.firstName) = lower(:fn)
               AND ( 
                   (:abha IS NOT NULL AND p.identifierType = 'ABHA' AND p.identifierNumber = :abha)
                    OR
                   (:mail IS NOT NULL AND lower(c.email) = lower(:mail)) 
               )
        """.trimIndent(),
        mapOf("fn" to first, "abha" to abhaNumber, "mail" to email)
    ).firstResult()
}
@ApplicationScoped class PatientContactRepository     : PanacheRepositoryBase<PatientContact, Long>
@ApplicationScoped class PatientAddressRepository     : PanacheRepositoryBase<PatientAddress, Long>
@ApplicationScoped class PatientAbhaRepository        : PanacheRepositoryBase<PatientAbha, Long>
@ApplicationScoped class BillingReferralRepository    : PanacheRepositoryBase<BillingReferral, Long>
@ApplicationScoped class EmergencyContactRepository   : PanacheRepositoryBase<EmergencyContact, Long>
@ApplicationScoped class InformationSharingRepository : PanacheRepositoryBase<InformationSharing, Long>
@ApplicationScoped class PatientInsuranceRepository  : PanacheRepositoryBase<PatientInsurance, Long>
@ApplicationScoped class ReferralRepository           : PanacheRepositoryBase<Referral, Long>
@ApplicationScoped class PatientRelationshipRepository: PanacheRepositoryBase<PatientRelationship, Long>
