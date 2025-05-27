CREATE TABLE facility (
    hospital_id        VARCHAR NOT NULL PRIMARY KEY,
    facility_name      VARCHAR,
    facility_type      VARCHAR,
    block              VARCHAR,
    phc_chc_name       VARCHAR,
    location           VARCHAR,
    officer_in_charge  VARCHAR,
    designation        VARCHAR,
    contact_number     VARCHAR,
    official_email     VARCHAR,
    network_id         VARCHAR,
    bed_strength       INTEGER,
    patient_types      VARCHAR,
    notes              TEXT,
    equipments         TEXT,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at         TIMESTAMP WITH TIME ZONE DEFAULT now()
);
