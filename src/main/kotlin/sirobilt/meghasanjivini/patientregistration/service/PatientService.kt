package sirobilt.meghasanjivini.patientregistration.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import org.jboss.logging.Logger
import sirobilt.meghasanjivini.patientregistration.dto.*
import sirobilt.meghasanjivini.patientregistration.model.*
import sirobilt.meghasanjivini.patientregistration.repository.*
import java.time.LocalDate
import java.util.*

@ApplicationScoped
class PatientService @Inject constructor(
    private val patientRepo: PatientRepository,
    private val contactRepo: PatientContactRepository,
    private val addressRepo: PatientAddressRepository,
    private val emergencyRepo: EmergencyContactRepository,
    private val insuranceRepo: PatientInsuranceRepository,

    private val tokenRepository: PatientTokenRepository,
    private val billingReferralRepo: BillingReferralRepository,
    private val infoSharingRepo: InformationSharingRepository,
    private val referralRepo: ReferralRepository,
    private val relationshipRepo: PatientRelationshipRepository,
    private val abhaRepo: PatientAbhaRepository,

) {

    private val logger: Logger = Logger.getLogger(PatientService::class.java)

    fun generateNextMrn(facilityId: String, lastMrn: String?): String {
        // Pad facilityId to 3 digits
        val paddedFacilityId = facilityId.padStart(3, '0')
        val paddedNetworkId: String = "00"

        // Extract last registration number from MRN (if any)
        val regNum = lastMrn
            ?.split("-")
            ?.takeIf { it.size == 4 }
            ?.let { it[2] + it[3] }
            ?.toLongOrNull() ?: 0L

        val nextRegNum = regNum + 1

        // Pad to 8 digits, then split into "0000-0001"
        val paddedRegNum = nextRegNum.toString().padStart(8, '0')
        val formattedRegNum = "${paddedRegNum.substring(0,4)}-${paddedRegNum.substring(4,8)}"


        return "$paddedFacilityId-$paddedNetworkId-$formattedRegNum"
    }
    @Transactional
    fun register(dto: PatientRegistrationDto): PatientResponseDto {
        logger.info("Patient registration started")

        val lastMrn = patientRepo.findLastMrnForFacility(dto.facilityId) // You need to implement this

// 2. Generate next MRN
        val mrn = generateNextMrn(dto.facilityId, lastMrn)

        // 1) create & persist the Patient
        val patient = dto.toEntity(mrn)
        patientRepo.persist(patient)

        // 2) persist contacts
        dto.contacts
            ?.map { it.toEntity(patient) }
            ?.also { contactEntities: List<PatientContact> ->
                contactRepo.persist(contactEntities)
                patient.contacts.addAll(contactEntities)
            }

        // 3) persist addresses
        dto.addresses
            ?.map { it.toEntity(patient) }
            ?.also { addressEntities: List<PatientAddress> ->
                addressRepo.persist(addressEntities)
                patient.addresses.addAll(addressEntities)
            }

        // 4) persist emergency contacts
        dto.emergencyContacts
            ?.map { it.toEntity(patient) }
            ?.also { emergencyEntities: List<EmergencyContact> ->
                emergencyRepo.persist(emergencyEntities)
                patient.emergencyContacts.addAll(emergencyEntities)
            }

        // 5) persist insurance (one-to-one)
        dto.insurance
            ?.toEntity(patient)
            ?.also { insuranceEntity: PatientInsurance ->
                insuranceRepo.persist(insuranceEntity)
                patient.insurance = insuranceEntity
            }

        // 6) persist ABHA (one-to-one)
        dto.abha
            ?.toEntity(patient)
            ?.also { abhaEntity: PatientAbha ->
                abhaRepo.persist(abhaEntity)
                patient.abha = abhaEntity
            }

        // 7) persist billing referral (one-to-one)
        dto.billingReferral
            ?.toEntity(patient)
            ?.also { billingReferralEntity: BillingReferral ->
                billingReferralRepo.persist(billingReferralEntity)
                patient.billingReferral = billingReferralEntity
            }

        // 8) persist information sharing (one-to-one)
        dto.informationSharing
            ?.toEntity(patient)
            ?.also { infoSharingEntity: InformationSharing ->
                infoSharingRepo.persist(infoSharingEntity)
                patient.informationSharing = infoSharingEntity
            }

        // 9) persist referrals
        dto.referrals
            ?.map { it.toEntity(patient) }
            ?.also { referralEntities: List<Referral> ->
                referralRepo.persist(referralEntities)
                patient.referrals.addAll(referralEntities)
            }

        // 10) persist relationships
        dto.relationships
            ?.map { it.toEntity(patient) }
            ?.also { relationshipEntities: List<PatientRelationship> ->
                relationshipRepo.persist(relationshipEntities)
                patient.relationships.addAll(relationshipEntities)
            }

        // 11) persist tokens from DTO
        dto.tokens
            ?.map { it.toEntity(patient) }
            ?.also { tokenEntities: List<PatientToken> ->
                tokenRepository.persist(tokenEntities)
                patient.tokens.addAll(tokenEntities)
            }



        // 13) return fully-populated DTO
        return patient.toDto()
    }


    @Transactional
    fun update(upId: String, dto: UpdatePatientDto): PatientResponseDto {
        val p = patientRepo.findByUpId(upId) ?: throw NotFoundException()

        // — Scalar fields —
        dto.facilityId     ?.let { p.facilityId     = it }
        dto.identifierType ?.let { p.identifierType = it }
        dto.identifierNumber?.let { p.identifierNumber = it }
        dto.title          ?.let { p.title          = it }
        dto.firstName      ?.let { p.firstName      = it }
        dto.middleName     ?.let { p.middleName     = it }
        dto.lastName       ?.let { p.lastName       = it }
        dto.dateOfBirth    ?.let { p.dateOfBirth    = it }
        dto.age            ?.let { p.age            = it }
        dto.gender         ?.let { p.gender         = it }
        dto.bloodGroup     ?.let { p.bloodGroup     = it }
        dto.maritalStatus  ?.let { p.maritalStatus  = it }
        dto.citizenship    ?.let { p.citizenship    = it }
        dto.religion       ?.let { p.religion       = it }
        dto.caste          ?.let { p.caste          = it }
        dto.occupation     ?.let { p.occupation     = it }
        dto.education      ?.let { p.education      = it }
        dto.annualIncome   ?.let { p.annualIncome   = it }


        // — One-to-many collections: clear + repopulate if provided —
        dto.contacts?.let { list ->
            p.contacts.apply {
                clear()
                list.forEach { c ->
                    add(PatientContact(
                        patient                = p,
                        mobileNumber           = c.mobileNumber.orEmpty(),
                        phoneNumber            = c.phoneNumber.orEmpty(),
                        email                  = c.email,
                        preferredContactMode   = c.preferredContactMode,
                        phoneContactPreference = c.phoneContactPreference,
                        consentToShare         = c.consentToShare
                    ))
                }
            }
        }

        dto.addresses?.let { list ->
            p.addresses.apply {
                clear()
                list.forEach { a ->
                    add(PatientAddress(
                        patient          = p,
                        addressType      = a.addressType,
                        houseNoOrFlatNo  = a.houseNoOrFlatNo,
                        localityOrSector = a.localityOrSector,
                        cityOrVillage    = a.cityOrVillage,
                        pincode          = a.pincode,
                        districtId       = a.districtId,
                        stateId          = a.stateId,
                        country          = a.country
                    ))
                }
            }
        }

        dto.emergencyContacts?.let { list ->
            p.emergencyContacts.apply {
                clear()
                list.forEach { e ->
                    add(EmergencyContact(
                        patient      = p,
                        contactName  = e.contactName,
                        relationship = e.relationship,
                        phoneNumber  = e.phoneNumber
                    ))
                }
            }
        }

        dto.referrals?.let { list ->
            p.referrals.apply {
                clear()
                list.forEach { r ->
                    add(Referral(
                        patient         = p,
                        fromFacilityId  = r.fromFacilityId,
                        toFacilityId    = r.toFacilityId,
                        referralDate    = r.referralDate,
                        reason          = r.reason
                    ))
                }
            }
        }

        dto.relationships?.let { list ->
            p.relationships.apply {
                clear()
                list.forEach { rel ->
                    add(PatientRelationship(
                        patient = p,
                        relativeId = rel.relativeId,
                        relationshipType = rel.relationshipType
                    ))
                }
            }
        }



        // — One-to-one associations: update existing or create new if provided —
        dto.billingReferral?.let { br ->
            p.billingReferral = p.billingReferral
                ?.apply {
                    billingType = br.billingType
                    referredBy  = br.referredBy
                }
                ?: BillingReferral(
                    patient     = p,
                    billingType = br.billingType,
                    referredBy  = br.referredBy
                )
        }

        dto.insurance?.let { ins ->
            p.insurance = p.insurance
                ?.apply {
                    insuranceProvider = ins.insuranceProvider
                    policyNumber      = ins.policyNumber
                    policyStartDate   = ins.policyStartDate
                    policyEndDate     = ins.policyEndDate
                    coverageAmount    = ins.coverageAmount
                }
                ?: PatientInsurance(
                    patient = p,
                    insuranceProvider = ins.insuranceProvider,
                    policyNumber = ins.policyNumber,
                    policyStartDate = ins.policyStartDate,
                    policyEndDate = ins.policyEndDate,
                    coverageAmount = ins.coverageAmount
                )
        }

        dto.abha?.let { a ->
            p.abha = p.abha
                ?.apply {
                    abhaNumber  = a.abhaNumber
                    abhaAddress = a.abhaAddress
                }
                ?: PatientAbha(
                    patient     = p,
                    abhaNumber  = a.abhaNumber,
                    abhaAddress = a.abhaAddress
                )
        }

        dto.informationSharing?.let { inf ->
            p.informationSharing = p.informationSharing
                ?.apply {
                    shareWithSpouse    = inf.shareWithSpouse
                    shareWithChildren  = inf.shareWithChildren
                    shareWithCaregiver = inf.shareWithCaregiver
                    shareWithOther     = inf.shareWithOther
                }
                ?: InformationSharing(
                    patient            = p,
                    shareWithSpouse    = inf.shareWithSpouse,
                    shareWithChildren  = inf.shareWithChildren,
                    shareWithCaregiver = inf.shareWithCaregiver,
                    shareWithOther     = inf.shareWithOther
                )
        }

        // No explicit persist needed if 'p' is still managed; Quarkus/Hibernate will flush at commit
        return p.toDto()
    }


    fun listAll(): List<PatientResponseDto> =
        patientRepo.findAll().list().map { it.toDto() }

    fun searchByQuery(query: String, page: Int, size: Int): List<Patient> {
        return patientRepo.searchByQuery(query, page, size)
    }

    fun countByQuery(query: String): Long {
        return patientRepo.countByQuery(query)
    }

    fun listAllWithCount(page: Int, size: Int): PatientListResponseDto {
        val pageResult = patientRepo.findAll().page(page, size)
        val totalCount = patientRepo.count()
        val patients = pageResult.list().map { it.toDto() }
        return PatientListResponseDto(
            patients = patients,
            totalCount = totalCount
        )
    }


    @Transactional
    fun delete(id: UUID) {
        if (!patientRepo.deleteById(id)) throw NotFoundException()
    }

    fun getById(id: UUID): PatientResponseDto =
        patientRepo.findById(id)?.toDto() ?: throw NotFoundException()

    fun exists(
        firstName: String,
        identifierType: IdentifierType,
        identifierNumber: String?,
        primaryEmail: String?
    ): Boolean {
        val abha = if (identifierType == IdentifierType.ABHA) identifierNumber else null
        return patientRepo.findDuplicate(firstName, abha, primaryEmail) != null
    }

    fun search(
        upId: String?,
        first: String?, last: String?,
        mobile: String?, email: String?,
        dobFrom: LocalDate?, dobTo: LocalDate?
    ): List<PatientResponseDto> =
        patientRepo.search(upId, first, last, mobile, email, dobFrom, dobTo)
            .map { it.toDto() }

    fun searchByCityOrName(city: String?, name: String?): List<PatientResponseDto> =
        patientRepo.searchByCityOrName(city, name)
            .map { it.toDto() }



}



fun Patient.toDto(): PatientResponseDto {
    val firstContact = contacts.firstOrNull()

    return PatientResponseDto(
        patientId = upId,
        facilityId = facilityId,
        identifierType = identifierType,
        identifierNumber = identifierNumber,
        title = title,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        fullName = listOfNotNull(firstName, middleName, lastName).joinToString(" "),
        dateOfBirth = dateOfBirth,
        age = age,
        gender = gender,
        bloodGroup = bloodGroup,
        maritalStatus = maritalStatus,
        citizenship = citizenship,
        religion = religion,
        caste = caste,
        occupation = occupation,
        education = education,
        annualIncome = annualIncome,
        registrationDate = registrationDate!!,
        isActive = isActive!!,
        isDeceased = isDeceased!!,
        phone = firstContact?.phoneNumber,
        email = firstContact?.email,

        contacts = contacts.map { it.toDto() },
        addresses = addresses?.map { it.toDto() },
        emergencyContacts = emergencyContacts?.map { it.toDto() },
        billingReferral = billingReferral?.toDto(),
        insurance = insurance?.toDto(),
        abha = abha?.toDto(),
        informationSharing = informationSharing?.toDto(),
        referrals = referrals?.map { it.toDto() },
        relationships = relationships?.map { it.toDto() },

    )
}

fun PatientContact.toDto() = ContactDto(
    mobileNumber = this.mobileNumber,
    phoneNumber = this.phoneNumber,
    email = this.email,
    preferredContactMode = this.preferredContactMode,
    phoneContactPreference = this.phoneContactPreference,
    consentToShare = this.consentToShare
)

fun PatientAddress.toDto() = AddressDto(
    addressType = this.addressType,
    houseNoOrFlatNo = this.houseNoOrFlatNo,
    localityOrSector = this.localityOrSector,
    cityOrVillage = this.cityOrVillage,
    pincode = this.pincode,
    districtId = this.districtId,
    stateId = this.stateId,
    country = this.country
)

fun EmergencyContact.toDto() = EmergencyContactDto(
    contactName = this.contactName,
    relationship = this.relationship,
    phoneNumber = this.phoneNumber
)

fun BillingReferral.toDto() = BillingReferralDto(
    billingType = this.billingType,
    referredBy = this.referredBy
)

fun PatientInsurance.toDto() = PatientInsuranceDto(
    insuranceProvider = this.insuranceProvider,
    policyNumber = this.policyNumber,
    policyStartDate = this.policyStartDate,
    policyEndDate = this.policyEndDate,
    coverageAmount = this.coverageAmount
)

fun PatientAbha.toDto() = AbhaDto(
    abhaNumber = this.abhaNumber,
    abhaAddress = this.abhaAddress
)

fun InformationSharing.toDto() = InformationSharingDto(
    shareWithSpouse = this.shareWithSpouse,
    shareWithChildren = this.shareWithChildren,
    shareWithCaregiver = this.shareWithCaregiver,
    shareWithOther = this.shareWithOther
)

fun Referral.toDto() = ReferralDto(
    fromFacilityId = this.fromFacilityId,
    toFacilityId = this.toFacilityId,
    referralDate = this.referralDate,
    reason = this.reason
)

fun PatientRelationship.toDto() = PatientRelationshipDto(
    relativeId = this.relativeId,
    relationshipType = this.relationshipType
)

fun PatientToken.toDto() = TokenDto(
    tokenNumber = this.tokenNumber,
    issueDate = this.issueDate?.toLocalDate(),
    expiryDate = this.expiryDate?.toLocalDate(),
    status = this.status,
    isRegistered = this.isRegistered,
    allocatedTo = this.allocatedTo
)

fun AbhaDto.toEntity(owner: Patient): PatientAbha =
    PatientAbha(
        patient     = owner,
        abhaNumber  = this.abhaNumber.orEmpty(),
        abhaAddress = this.abhaAddress.orEmpty()
    )

fun BillingReferralDto.toEntity(owner: Patient): BillingReferral =
    BillingReferral(
        patient     = owner,
        billingType = this.billingType,
        referredBy  = this.referredBy.orEmpty()
    )

fun InformationSharingDto.toEntity(owner: Patient): InformationSharing =
    InformationSharing(
        patient           = owner,
        shareWithSpouse   = this.shareWithSpouse,
        shareWithChildren = this.shareWithChildren,
        shareWithCaregiver= this.shareWithCaregiver,
        shareWithOther    = this.shareWithOther
    )

fun ReferralDto.toEntity(owner: Patient): Referral =
    Referral(
        patient        = owner,
        fromFacilityId = this.fromFacilityId,
        toFacilityId   = this.toFacilityId,
        referralDate   = this.referralDate,
        reason         = this.reason.orEmpty()
    )

fun PatientRelationshipDto.toEntity(owner: Patient): PatientRelationship =
    PatientRelationship(
        patient = owner,
        relativeId = this.relativeId,
        relationshipType = this.relationshipType
    )

fun TokenDto.toEntity(owner: Patient): PatientToken =
    PatientToken(
        patient      = owner,
        tokenNumber  = this.tokenNumber,
        issueDate    = this.issueDate?.atStartOfDay()?.let { java.time.OffsetDateTime.of(it, java.time.ZoneOffset.UTC) }
            ?: java.time.OffsetDateTime.now(),
        expiryDate   = this.expiryDate?.atStartOfDay()?.let { java.time.OffsetDateTime.of(it, java.time.ZoneOffset.UTC) }
            ?: java.time.OffsetDateTime.now().plusDays(1),
        status       = this.status,
        isRegistered = this.isRegistered,
        allocatedTo  = this.allocatedTo
    )