CREATE TABLE patients (
    patient_id         VARCHAR PRIMARY KEY,
    facility_id        VARCHAR,
    identifier_type    VARCHAR,
    identifier_number  VARCHAR,
    title              VARCHAR,
    first_name         VARCHAR,
    middle_name        VARCHAR,
    last_name          VARCHAR,
    date_of_birth      DATE,
    age                INTEGER,
    gender             VARCHAR,
    blood_group        VARCHAR,
    marital_status     VARCHAR,
    citizenship        VARCHAR,
    religion           VARCHAR,
    caste              VARCHAR,
    occupation         VARCHAR,
    education          VARCHAR,
    annual_income      VARCHAR,
    registration_date  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    is_active          BOOLEAN DEFAULT true,
    is_deceased        BOOLEAN DEFAULT false,
    soft_deleted        BOOLEAN DEFAULT false,

    CONSTRAINT fk_patients_facility
      FOREIGN KEY (facility_id)
      REFERENCES facility(hospital_id)
      ON DELETE RESTRICT
      ON UPDATE CASCADE
);

CREATE TABLE patient_contacts (
    contact_id             SERIAL PRIMARY KEY,
    patient_id             VARCHAR,
    mobile_number          VARCHAR,
    phone_number           VARCHAR,
    email                  VARCHAR,
    preferred_contact_mode VARCHAR,
    phone_contact_preference VARCHAR,
    consent_to_share       BOOLEAN DEFAULT false,

    CONSTRAINT fk_patient_contacts_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE emergency_contacts (
    emergency_contact_id   SERIAL PRIMARY KEY,
    patient_id             VARCHAR,
    contact_name           VARCHAR,
    relationship           VARCHAR,
    phone_number           VARCHAR,

    CONSTRAINT fk_emergency_contacts_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE patient_addresses (
    address_id         SERIAL PRIMARY KEY,
    patient_id         VARCHAR,
    address_type       VARCHAR,
    house_no_or_flat_no VARCHAR,
    locality_or_sector VARCHAR,
    city_or_village    VARCHAR,
    pincode           VARCHAR,
    district_id        VARCHAR,
    state_id           VARCHAR,
    country            VARCHAR DEFAULT 'India',

    CONSTRAINT fk_patient_addresses_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE patient_abha (
    abha_id        SERIAL PRIMARY KEY,
    patient_id     VARCHAR,
    abha_number    VARCHAR,
    abha_address   VARCHAR,

    CONSTRAINT fk_patient_abha_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE billing_referral (
    billing_id     SERIAL PRIMARY KEY,
    patient_id     VARCHAR,
    billing_type   VARCHAR,
    referred_by    VARCHAR,

    CONSTRAINT fk_billing_referral_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE information_sharing (
    share_id               SERIAL PRIMARY KEY,
    patient_id             VARCHAR,
    share_with_spouse      BOOLEAN DEFAULT false,
    share_with_children    BOOLEAN DEFAULT false,
    share_with_caregiver   BOOLEAN DEFAULT false,
    share_with_other       BOOLEAN DEFAULT false,

    CONSTRAINT fk_information_sharing_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE patient_insurance (
    insurance_id     SERIAL PRIMARY KEY,
    patient_id       VARCHAR UNIQUE, -- Only one insurance per patient
    insurance_provider VARCHAR,
    policy_number    VARCHAR,
    policy_start_date DATE,
    policy_end_date   DATE,
    coverage_amount  NUMERIC,

    CONSTRAINT fk_patient_insurance_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE referrals (
    referral_id     SERIAL PRIMARY KEY,
    patient_id      VARCHAR,
    from_facility_id VARCHAR,
    to_facility_id   VARCHAR,
    referral_date    DATE DEFAULT CURRENT_DATE,
    reason           VARCHAR,

    CONSTRAINT fk_referrals_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);

CREATE TABLE patient_relationships (
    relationship_id   SERIAL PRIMARY KEY,
    patient_id        VARCHAR,
    relative_id       UUID,
    relationship_type VARCHAR,

    CONSTRAINT fk_patient_relationships_patient
      FOREIGN KEY (patient_id)
      REFERENCES patients(patient_id)
      ON DELETE CASCADE
);


