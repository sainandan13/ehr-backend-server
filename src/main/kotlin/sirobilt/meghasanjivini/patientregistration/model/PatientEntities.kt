package sirobilt.meghasanjivini.patientregistration.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.SoftDelete
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * NOTE: Ensure the Kotlin no-arg compiler plugin is enabled for JPA entities
 * (e.g., kotlin("plugin.noarg") with annotation "jakarta.persistence.Entity").
 * With that in place, explicit no-arg constructors are not required.
 */

@Entity
@Table(name = "patients")
class Patient(
    @Id @Column(name = "patient_id")
    var upId: String = "",

    @Column(name = "facility_id")
    var facilityId: String = "",

    @Enumerated(EnumType.STRING)
    var identifierType: IdentifierType = IdentifierType.ABHA,
    var identifierNumber: String = "",

    @Enumerated(EnumType.STRING)
    var title: Title? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var dateOfBirth: LocalDate? = null,
    var age: Int? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,
    @Enumerated(EnumType.STRING)
    var bloodGroup: BloodGroup? = null,
    @Enumerated(EnumType.STRING)
    var maritalStatus: MaritalStatus? = null,

    var citizenship: String? = null,
    var religion: String? = null,
    var caste: String? = null,
    var occupation: String? = null,
    var education: String? = null,
    var annualIncome: String? = null,

    var registrationDate: OffsetDateTime = OffsetDateTime.now(),
    var isActive: Boolean = true,
    var isDeceased: Boolean = false,


    @Column(nullable = false)
    var softDeleted: Boolean = false,

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var contacts: MutableList<PatientContact> = mutableListOf(),

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var addresses: MutableList<PatientAddress> = mutableListOf(),

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var emergencyContacts: MutableList<EmergencyContact> = mutableListOf(),

    @OneToOne(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var billingReferral: BillingReferral? = null,

    @OneToOne(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var insurance: PatientInsurance? = null,

    @OneToOne(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var abha: PatientAbha? = null,

    @OneToOne(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var informationSharing: InformationSharing? = null,

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var referrals: MutableList<Referral> = mutableListOf(),

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var relationships: MutableList<PatientRelationship> = mutableListOf(),

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var tokens: MutableList<PatientToken> = mutableListOf()
) {
    override fun toString(): String = "Patient(upId=$upId, name=$firstName $lastName)"
}

@Entity
@Table(name = "patient_contacts")
class PatientContact(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var contactId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var mobileNumber: String? = null,
    var phoneNumber: String = "",
    var email: String? = null,

    @Enumerated(EnumType.STRING)
    var preferredContactMode: ContactMode? = null,
    @Enumerated(EnumType.STRING)
    var phoneContactPreference: PhonePref? = null,
    var consentToShare: Boolean = false
) {
    override fun toString(): String = "PatientContact(id=$contactId, mobile=$mobileNumber)"
}

@Entity
@Table(name = "emergency_contacts")
class EmergencyContact(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var emergencyContactId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var contactName: String? = null,
    var relationship: String? = null,
    var phoneNumber: String? = null
) {
    override fun toString(): String = "EmergencyContact(id=$emergencyContactId, name=$contactName)"
}

@Entity
@Table(name = "patient_addresses")
class PatientAddress(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var addressId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    @Enumerated(EnumType.STRING)
    var addressType: AddressType = AddressType.Present,
    var houseNoOrFlatNo: String? = null,
    var localityOrSector: String? = null,
    var cityOrVillage: String? = null,
    var pincode: String? = null,
    var districtId: String? = null,
    var stateId: String? = null,
    var country: String? = "India"
) {
    override fun toString(): String = "PatientAddress(id=$addressId, city=$cityOrVillage)"
}

@Entity
@Table(name = "patient_abha")
class PatientAbha(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var abhaId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var abhaNumber: String? = null,
    var abhaAddress: String? = null
) {
    override fun toString(): String = "PatientAbha(id=$abhaId, number=$abhaNumber)"
}

@Entity
@Table(name = "billing_referral")
class BillingReferral(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var billingId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    @Enumerated(EnumType.STRING)
    var billingType: BillingType = BillingType.General,
    var referredBy: String? = null
) {
    override fun toString(): String = "BillingReferral(id=$billingId, type=$billingType)"
}

@Entity
@Table(name = "information_sharing")
class InformationSharing(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var shareId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var shareWithSpouse: Boolean = false,
    var shareWithChildren: Boolean = false,
    var shareWithCaregiver: Boolean = false,
    var shareWithOther: Boolean = false
) {
    override fun toString(): String = "InformationSharing(id=$shareId)"
}

@Entity
@Table(name = "patient_insurance")
class PatientInsurance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var insuranceId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var insuranceProvider: String? = null,
    var policyNumber: String? = null,
    var policyStartDate: LocalDate? = LocalDate.now(),
    var policyEndDate: LocalDate? = LocalDate.now(),
    var coverageAmount: BigDecimal? = BigDecimal.ZERO
) {
    override fun toString(): String = "PatientInsurance(id=$insuranceId, provider=$insuranceProvider)"
}

@Entity
@Table(name = "referrals")
class Referral(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var referralId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var fromFacilityId: String? = null,
    var toFacilityId: String? = null,
    var referralDate: LocalDate = LocalDate.now(),
    var reason: String? = null
) {
    override fun toString(): String = "Referral(id=$referralId, from=$fromFacilityId, to=$toFacilityId)"
}

@Entity
@Table(name = "patient_relationships")
class PatientRelationship(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var relationshipId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var relativeId: UUID? = null,
    @Enumerated(EnumType.STRING)
    var relationshipType: RelationType? = null
) {
    override fun toString(): String = "PatientRelationship(id=$relationshipId, type=$relationshipType)"
}

@Entity
@Table(name = "patient_tokens")
class PatientToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var tokenId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    var patient: Patient? = null,

    var tokenNumber: String = "",
    var issueDate: OffsetDateTime? = OffsetDateTime.now(),
    var expiryDate: OffsetDateTime? = OffsetDateTime.now().plusDays(1),
    @Enumerated(EnumType.STRING)
    var status: TokenStatus = TokenStatus.Active,
    var isRegistered: Boolean = false,
    var allocatedTo: String? = null
) {
    override fun toString(): String = "PatientToken(id=$tokenId, number=$tokenNumber)"
}
