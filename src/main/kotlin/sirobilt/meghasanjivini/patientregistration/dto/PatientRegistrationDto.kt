package sirobilt.meghasanjivini.patientregistration.dto

import sirobilt.meghasanjivini.patientregistration.model.EmergencyContact
import sirobilt.meghasanjivini.patientregistration.model.Patient
import sirobilt.meghasanjivini.patientregistration.model.PatientAddress
import sirobilt.meghasanjivini.patientregistration.model.PatientContact
import sirobilt.meghasanjivini.patientregistration.model.PatientInsurance

/* PatientRegistrationDto.kt */
fun PatientRegistrationDto.toEntity(mrn : String): Patient {
    val p = Patient(
        upId = mrn,
        facilityId       = facilityId,
        identifierType   = identifierType,
        identifierNumber = identifierNumber,
        firstName        = firstName,
        middleName       = middleName,
        lastName         = lastName,
        dateOfBirth      = dateOfBirth,
        gender           = gender,

        /* ---- static optional columns ---- */
        title            = title,
        age              = age,
        bloodGroup       = bloodGroup,
        maritalStatus    = maritalStatus,
        citizenship      = citizenship,
        religion         = religion,
        caste            = caste,
        occupation       = occupation,
        education        = education,
        annualIncome     = annualIncome
    )


    return p
}

/* ContactDto.kt */
fun ContactDto.toEntity(owner: Patient) = PatientContact(
    patient = owner,
    mobileNumber = mobileNumber,
    phoneNumber = phoneNumber,
    email = email,
    preferredContactMode = preferredContactMode,
    phoneContactPreference = phoneContactPreference,
    consentToShare = consentToShare
)

/* AddressDto.kt */
fun AddressDto.toEntity(owner: Patient) = PatientAddress(
    patient = owner,
    addressType = addressType,
    houseNoOrFlatNo = houseNoOrFlatNo,
    localityOrSector = localityOrSector,
    cityOrVillage = cityOrVillage,
    pincode = pincode,
    districtId = districtId,
    stateId = stateId,
    country = country
)

/* EmergencyContactDto.kt */
fun EmergencyContactDto.toEntity(owner: Patient) = EmergencyContact(
    patient = owner,
    contactName = contactName,
    relationship = relationship,
    phoneNumber = phoneNumber
)

/* PatientInsuranceDto.kt */
fun PatientInsuranceDto.toEntity(owner: Patient) = PatientInsurance(
    patient = owner,
    insuranceProvider = insuranceProvider,
    policyNumber = policyNumber,
    policyStartDate = policyStartDate,
    policyEndDate = policyEndDate,
    coverageAmount = coverageAmount
)
