-- =========================================================================
-- 1) USER & FARM
-- =========================================================================
INSERT INTO users (
    fullname,
    password_hash,
    phone,
    email
) VALUES (
    'Ganesh Patil',
    'password123',
    '9999999999',
    'ganesh@example.com'
);

INSERT INTO farm (
    farm_name,
    location,
    size,
    goat_types,
    farmer_id
) VALUES (
    'Green Valley Farm',
    'Pune, Maharashtra',
    50,
    'BEETAL, BOER, JAMNAPARI, SOJAT',
    1
);

-- =========================================================================
-- 2) GOATS: FULL 3-LEVEL PEDIGREE FOR G4001 & HERD MEMBERS
-- =========================================================================

-- -------------------------------------------------------------------------
-- Generation 3 Ancestors (Grandparents for G4001)
-- -------------------------------------------------------------------------
-- Paternal Grand Sire
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B001', 'BEETAL', 'MALE', '2019-02-15', 78.5, 'HEALTHY', NULL, NULL, 84.0, NULL, NULL, 1);

-- Paternal Grand Dam
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B002', 'BEETAL', 'FEMALE', '2019-04-10', 62.0, 'HEALTHY', NULL, NULL, 74.0, 2.8, 2, 1);

-- Maternal Grand Sire
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B003', 'JAMNAPARI', 'MALE', '2019-01-20', 82.0, 'HEALTHY', NULL, NULL, 88.0, NULL, NULL, 1);

-- Maternal Grand Dam
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('G203', 'SOJAT', 'FEMALE', '2019-05-18', 64.0, 'HEALTHY', NULL, NULL, 76.0, 3.0, 3, 1);

-- Additional Grandparents for Collateral Lines
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B004', 'BOER', 'FEMALE', '2019-06-12', 60.0, 'HEALTHY', NULL, NULL, 72.0, 2.1, 2, 1),
       ('B005', 'JAMNAPARI', 'MALE', '2019-03-25', 85.0, 'HEALTHY', NULL, NULL, 89.0, NULL, NULL, 1),
       ('B006', 'JAMNAPARI', 'FEMALE', '2019-07-14', 63.5, 'HEALTHY', NULL, NULL, 75.0, 2.7, 1, 1),
       ('G201', 'BEETAL', 'FEMALE', '2020-03-15', 55.0, 'HEALTHY', NULL, NULL, 70.0, 2.4, 1, 1),
       ('G202', 'BOER', 'FEMALE', '2020-04-18', 58.0, 'HEALTHY', NULL, NULL, 71.0, 2.2, 2, 1),
       ('G204', 'OSMANABADI', 'FEMALE', '2020-05-20', 50.0, 'HEALTHY', NULL, NULL, 68.0, 1.9, 1, 1),
       ('G205', 'BARBARI', 'FEMALE', '2020-06-05', 42.0, 'HEALTHY', NULL, NULL, 62.0, 1.6, 1, 1),
       ('G206', 'SAANEN', 'FEMALE', '2020-07-22', 65.0, 'HEALTHY', NULL, NULL, 76.0, 3.8, 2, 1);

-- -------------------------------------------------------------------------
-- Generation 2: Parents (Sire & Dam for G4001) & Herd Breeding Stock
-- -------------------------------------------------------------------------
-- Sire (Father of G4001) -> Points to B001 & B002
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B1001', 'BEETAL', 'MALE', '2020-08-12', 72.0, 'HEALTHY', 'B001', 'B002', 82.0, NULL, NULL, 1);

-- Dam (Mother of G4001) -> Points to B003 & G203
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('M1001', 'JAMNAPARI', 'FEMALE', '2020-11-05', 58.5, 'HEALTHY', 'B003', 'G203', 73.0, 2.6, 2, 1);

-- Secondary Sire & Breeding Bucks
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('B201', 'BEETAL', 'MALE', '2022-07-05', 58.0, 'HEALTHY', 'B001', 'B002', 80.0, NULL, NULL, 1),
       ('B202', 'BOER', 'MALE', '2022-06-12', 62.0, 'HEALTHY', 'B003', 'B004', 85.0, NULL, NULL, 1),
       ('B203', 'OSMANABADI', 'MALE', '2022-05-10', 56.0, 'HEALTHY', 'B001', 'G204', 79.0, NULL, NULL, 1),
       ('B204', 'JAMNAPARI', 'MALE', '2022-09-15', 65.0, 'HEALTHY', 'B005', 'B006', 90.0, NULL, NULL, 1),
       ('B205', 'BEETAL', 'MALE', '2022-08-15', 59.0, 'HEALTHY', 'B204', 'G204', 82.0, NULL, NULL, 1),
       ('B1002', 'BEETAL', 'MALE', '2021-02-10', 70.0, 'HEALTHY', 'B001', 'B002', 81.0, NULL, NULL, 1),
       ('B1003', 'BOER', 'MALE', '2021-03-14', 74.0, 'HEALTHY', 'B003', 'B004', 83.0, NULL, NULL, 1),
       ('B1004', 'JAMNAPARI', 'MALE', '2021-04-18', 76.0, 'HEALTHY', 'B005', 'B006', 86.0, NULL, NULL, 1),
       ('B1005', 'BEETAL', 'MALE', '2021-05-22', 71.0, 'HEALTHY', 'B001', 'G201', 82.0, NULL, NULL, 1);

-- -------------------------------------------------------------------------
-- Generation 1: Target Goat G4001, Siblings, and Delivery Cohort
-- -------------------------------------------------------------------------
-- Main Target Goat (Full 3-level tree: G4001 -> B1001/M1001 -> B001/B002/B003/G203)
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('G4001', 'BEETAL', 'FEMALE', '2022-05-24', 50.0, 'HEALTHY', 'B1001', 'M1001', 70.0, 2.5, 2, 1);

-- Related Herd & Inbreeding test examples
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('G101', 'BEETAL', 'FEMALE', '2023-01-15', 35.0, 'HEALTHY', 'B201', 'G201', 65.0, 2.5, 1, 1),
       ('G102', 'BOER', 'FEMALE', '2023-02-20', 32.0, 'HEALTHY', 'B202', 'G202', 60.0, 2.2, 2, 1),
       ('G103', 'SOJAT', 'FEMALE', '2023-03-10', 30.0, 'HEALTHY', 'B201', 'G203', 62.0, 2.0, 1, 1),
       ('G104', 'OSMANABADI', 'FEMALE', '2023-04-18', 28.0, 'HEALTHY', 'B203', 'G204', 58.0, 1.8, 1, 1),
       ('G105', 'BARBARI', 'FEMALE', '2023-05-02', 27.0, 'HEALTHY', 'B202', 'G205', 55.0, 1.5, 0, 1),
       ('G106', 'SAANEN', 'FEMALE', '2023-06-28', 29.0, 'HEALTHY', 'B204', 'G206', 57.0, 2.3, 1, 1),
       ('B107', 'BEETAL', 'MALE', '2023-07-12', 31.0, 'HEALTHY', 'B201', 'G204', 61.0, 2.1, 1, 1);

-- Upcoming/Overdue Delivery Goats
INSERT INTO goat (tag_number, breed, gender, birth_date, weight, health_status, father_tag_number, mother_tag_number, height, milk_per_day, last_kid_count, farm_id)
VALUES ('G4002', 'BEETAL', 'FEMALE', '2022-06-10', 48.0, 'HEALTHY', 'B1001', 'G201', 68.0, 2.2, 1, 1),
       ('G4003', 'BOER', 'FEMALE', '2022-07-14', 52.0, 'HEALTHY', 'B202', 'G202', 69.0, 2.4, 2, 1),
       ('G4004', 'JAMNAPARI', 'FEMALE', '2022-08-01', 54.0, 'HEALTHY', 'B204', 'M1001', 71.0, 2.6, 2, 1),
       ('G4005', 'BEETAL', 'FEMALE', '2022-09-19', 49.0, 'HEALTHY', 'B1001', 'B002', 67.0, 2.0, 1, 1);

-- =========================================================================
-- 3) BREEDING RECORDS
-- =========================================================================

-- Completed deliveries for G4001 (2 cycles)
INSERT INTO Breeding_Records (
    breeding_date, pregnancy_status, offspring_count, goat_tag_number, breeder_tag_number,
    expected_kidding_date, delivery_date, kids_alive, kids_dead, goat_id, mate_id, farm_id
) VALUES
('2024-01-10', 'DELIVERED', 2, 'G4001', 'B204', '2024-06-08', '2024-06-07', 2, 0,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4001'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B204'), 1),

('2025-02-15', 'DELIVERED', 1, 'G4001', 'B202', '2025-07-15', '2025-07-14', 1, 0,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4001'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B202'), 1);

-- Historical Herd Cycles
INSERT INTO Breeding_Records (
    breeding_date, pregnancy_status, offspring_count, goat_tag_number, breeder_tag_number,
    expected_kidding_date, delivery_date, kids_alive, kids_dead, goat_id, mate_id, farm_id
) VALUES
('2024-01-01', 'DELIVERED', 2, 'G101', 'B201', '2024-05-28', '2024-05-28', 2, 0,
 (SELECT goat_id FROM goat WHERE tag_number = 'G101'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B201'), 1),

('2024-01-20', 'DELIVERED', 3, 'G103', 'B201', '2024-06-18', '2024-06-19', 2, 1,
 (SELECT goat_id FROM goat WHERE tag_number = 'G103'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B201'), 1),

('2024-02-20', 'DELIVERED', 1, 'G106', 'B204', '2024-07-19', '2024-07-18', 1, 0,
 (SELECT goat_id FROM goat WHERE tag_number = 'G106'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B204'), 1);

-- Active / Overdue / Upcoming Delivery Window
INSERT INTO Breeding_Records (
    breeding_date, pregnancy_status, offspring_count, goat_tag_number, breeder_tag_number,
    expected_kidding_date, kids_alive, kids_dead, delivery_date, goat_id, mate_id, farm_id
) VALUES
-- OVERDUE (5 days before today)
(DATEADD('DAY', -155, CURRENT_DATE), 'PREGNANT', NULL, 'G4002', 'B1002',
 DATEADD('DAY', -5, CURRENT_DATE), NULL, NULL, NULL,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4002'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B1002'), 1),

-- UPCOMING (2 days from today - Imminent)
(DATEADD('DAY', -148, CURRENT_DATE), 'PREGNANT', NULL, 'G4003', 'B1003',
 DATEADD('DAY', 2, CURRENT_DATE), NULL, NULL, NULL,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4003'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B1003'), 1),

-- UPCOMING (6 days from today - In window)
(DATEADD('DAY', -144, CURRENT_DATE), 'PREGNANT', NULL, 'G4004', 'B1004',
 DATEADD('DAY', 6, CURRENT_DATE), NULL, NULL, NULL,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4004'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B1004'), 1),

-- FUTURE (15 days from today)
(DATEADD('DAY', -135, CURRENT_DATE), 'PREGNANT', NULL, 'G4005', 'B1005',
 DATEADD('DAY', 15, CURRENT_DATE), NULL, NULL, NULL,
 (SELECT goat_id FROM goat WHERE tag_number = 'G4005'),
 (SELECT goat_id FROM goat WHERE tag_number = 'B1005'), 1);

-- =========================================================================
-- 4) VACCINATION RECORDS: 2 FULL YEARS FOR G4001 (2024 - 2026)
-- =========================================================================

-- Year 1 (2024 - 2025)
--INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
--VALUES
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2024-01-15', 'Dr. Vet', '2 ml (SC)', 'Bi-annual schedule', 'ET', '2024-07-15', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'FMD', '2024-04-15', 'Dr. Vet', '2 ml (IM)', 'Spring bi-annual round', 'FMD', '2024-10-15', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'HS', '2024-05-10', 'Dr. Vet', '2 ml (SC)', 'Pre-monsoon dose', 'HS', '2025-05-10', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2024-07-15', 'Dr. Vet', '2 ml (SC)', 'Monsoon booster', 'ET', '2025-01-15', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'PPR', '2024-09-10', 'Dr. Vet', '1 ml (SC)', 'Annual dose', 'PPR', '2025-09-10', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'FMD', '2024-10-15', 'Dr. Vet', '2 ml (IM)', 'Pre-winter booster', 'FMD', '2025-04-15', 1),
--((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'GOAT_POX', '2024-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter annual dose', 'GOAT_POX', '2025-10-20', 1);

-- Year 2 (2025 - 2026)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2025-01-15', 'Dr. Vet', '2 ml (SC)', 'Bi-annual schedule', 'ET', '2025-07-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'FMD', '2025-04-15', 'Dr. Vet', '2 ml (IM)', 'Spring round', 'FMD', '2025-10-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'HS', '2025-05-10', 'Dr. Vet', '2 ml (SC)', 'Pre-monsoon dose', 'HS', '2026-05-10', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2025-07-15', 'Dr. Vet', '2 ml (SC)', 'Monsoon dose', 'ET', '2026-01-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'PPR', '2025-09-10', 'Dr. Vet', '1 ml (SC)', 'Annual dose', 'PPR', '2026-09-10', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'FMD', '2025-10-15', 'Dr. Vet', '2 ml (IM)', 'Pre-winter booster', 'FMD', '2026-04-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'GOAT_POX', '2025-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter dose', 'GOAT_POX', '2026-10-20', 1);

-- Recent 2026 Administrations & Pending Due Boosters
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2026-01-15', 'Dr. Vet', '2 ml (SC)', 'Routine dose', 'ET', '2026-07-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'FMD', '2026-04-15', 'Dr. Vet', '2 ml (IM)', 'Spring round', 'FMD', '2026-10-15', 1),
((SELECT goat_id FROM goat WHERE tag_number = 'G4001'), 'ET', '2026-06-14', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2026-12-14', 1),
-- Overdue Booster (Due yesterday)
((SELECT goat_id FROM goat WHERE tag_number = 'G4002'), 'ET', DATEADD('DAY', -180, CURRENT_DATE), 'Dr. Vet', '2 ml (SC)', 'Past regular dose', 'ET', DATEADD('DAY', -1, CURRENT_DATE), 1),
-- Upcoming Booster (Due in 3 days)
((SELECT goat_id FROM goat WHERE tag_number = 'G4003'), 'FMD', DATEADD('DAY', -177, CURRENT_DATE), 'Dr. Vet', '2 ml (IM)', 'Previous dose', 'FMD', DATEADD('DAY', 3, CURRENT_DATE), 1);