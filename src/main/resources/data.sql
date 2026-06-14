INSERT INTO Users (username, password_hash, role, email, phone)
VALUES ('dev', 'demo_hash_123', 'FARMER', 'ganesh@example.com', '9999999999');

INSERT INTO users (user_id,
                   full_name,
                   username,
                   password_hash,
                   phone,
                   email,
                   role)
VALUES (1,
        'Ganesh Patil',
        'ganesh',
        'password123',
        '9999999999',
        'ganesh@example.com',
        'ADMIN');

-- FARM (link to farmer)
-- H2 supports SCOPE_IDENTITY() to fetch last generated ID
-- -------------------------------------------------------------
INSERT INTO farm (farm_id,
                  farm_name,
                  location,
                  size,
                  goat_types,
                  farmer_id)
VALUES (1,
        'Green Valley Farm',
        'Pune, Maharashtra',
        50,
        'BEETAL, BOER',
        1);

-- INSERT INTO farm (farm_name, location, size, goat_types)
-- VALUES ('Ganesh Goat Farm', 'Pune', 2.5, 'Boer,Beetal');

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G101', 'BEETAL', 'FEMALE', '2023-01-15', 35, 'HEALTHY', 'B201', 'G201', 65, 2.5, 1, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G102', 'BOER', 'FEMALE', '2023-02-20', 32, 'HEALTHY', 'B202', 'G202', 60, 2.2, 2, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G103', 'SOJAT', 'FEMALE', '2023-03-10', 30, 'HEALTHY', 'B201', 'G203', 62, 2.0, 1, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('B201', 'BEETAL', 'MALE', '2022-07-05', 58, 'HEALTHY', 'B001', 'B002', 80, NULL, NULL, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('B202', 'BOER', 'MALE', '2022-06-12', 62, 'HEALTHY', 'B003', 'B004', 85, NULL, NULL, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G104', 'OSMANABADI', 'FEMALE', '2023-04-18', 28, 'HEALTHY', 'B203', 'G204', 58, 1.8, 1, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G105', 'BARBARI', 'FEMALE', '2023-05-02', 27, 'HEALTHY', 'B202', 'G205', 55, 1.5, 0, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('B204', 'JAMNAPARI', 'MALE', '2022-09-15', 65, 'HEALTHY', 'B005', 'B006', 90, NULL, NULL, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G106', 'SAANEN', 'FEMALE', '2023-06-28', 29, 'HEALTHY', 'B204', 'G206', 57, 2.3, 1, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('B107', 'BEETAL', 'MALE', '2023-07-12', 31, 'HEALTHY', 'B203', 'G204', 61, 2.1, 1, 1);

INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('B205', 'BEETAL', 'MALE', '2022-08-15', 59, 'HEALTHY', 'B204', 'G204', 82, NULL, NULL, 1);

INSERT INTO breeding_records (breeding_date,
                              pregnancy_status,
                              offspring_count,
                              goat_tag_number,
                              breeder_tag_number,
                              expected_kidding_date,
                              goat_id,
                              mate_id,
                              farm_id)
VALUES ('2024-01-01', 'PREGNANT', 2, 'G101', 'B201', '2024-05-28', 1, 4, 1),
       ('2024-01-10', 'PREGNANT', 0, 'G102', 'B202', '2024-06-08', 2, 5, 1),
       ('2024-01-20', 'PREGNANT', 3, 'G103', 'B201', '2024-06-18', 3, 4, 1),
       ('2024-01-25', 'PREGNANT', 0, 'G104', 'B202', '2024-06-23', 6, 4, 1),
       ('2024-02-10', 'PREGNANT', 0, 'G105', 'B202', '2024-07-09', 7, 5, 1),
       ('2024-02-20', 'PREGNANT', 1, 'G106', 'B204', '2024-07-19', 9, 8, 1),
       ('2024-03-10', 'PREGNANT', 2, 'G103', 'B201', '2024-08-07', 3, 4, 1),
       ('2024-03-20', 'NOT_PREGNANT', 0, 'G102', 'B202', '2024-08-17', 2, 4, 1),
       ('2024-04-01', 'PREGNANT', 1, 'G103', 'B203', '2024-08-29', 3, 4, 1),
       ('2024-04-10', 'UNKNOWN', 0, 'G105', 'B202', '2024-09-07', 7, 5, 1),
       ('2024-04-20', 'PREGNANT', 3, 'G106', 'B204', '2024-09-17', 9, 8, 1);

INSERT INTO vaccination_record (goat_id,
                                vaccine_name,
                                vaccination_date,
                                administered_by,
                                dosage,
                                remarks,
                                next_vaccine_name,
                                next_vaccination_date,
                                farm_id)
VALUES (1, 'PPR', '2024-01-10', 'Dr. Sharma', '2 ml', 'Routine', 'FMD', '2024-07-15', 1),
       (2, 'FMD', '2024-01-15', 'Dr. Sharma', '2 ml', 'Routine', 'FMD Booster', '2024-08-07', 1),
       (3, 'ET', '2024-01-20', 'Dr. Kumar', '1 ml (SC)', 'Routine', 'PPR', '2024-07-20', 1),
       (4, 'PPR', '2024-01-25', 'Dr. Singh', '2 ml', 'Routine', 'PPR Booster', '2024-08-25', 1),
       (5, 'FMD', '2024-02-01', 'Dr. Sharma', '2 ml', 'Routine', 'FMD', '2024-06-06', 1),
       (6, 'ET', '2024-02-05', 'Dr. Singh', '1 ml (SC)', 'Routine', 'ET', '2024-05-10', 1),
       (7, 'PPR', '2024-02-10', 'Dr. Kumar', '2 ml', 'Routine', 'FMD', '2024-05-10', 1),
       (8, 'FMD', '2024-02-15', 'Dr. Sharma', '2 ml', 'Routine', 'FMD', '2024-05-25', 1),
       (9, 'ET', '2024-02-20', 'Dr. Kumar', '1 ml (SC)', 'Routine', 'PPR', '2024-05-25', 1),
       (10, 'PPR', '2024-02-25', 'Dr. Sharma', '2 ml', 'Routine', 'FMD', '2024-06-02', 1);

-- =========================================================================
-- 1) GOAT (DOB exactly 4 years old as of 2026-05-24)
-- =========================================================================
INSERT INTO goat (tag_number,
                  breed,
                  gender,
                  birth_date,
                  weight,
                  health_status,
                  father_tag_number,
                  mother_tag_number,
                  height,
                  milk_per_day,
                  last_kid_count,
                  farm_id)
VALUES ('G4001',      -- tagNumber
        'BEETAL',     -- breed
        'FEMALE',     -- female tag starts with G
        '2022-05-24', -- DOB (4 years old)
        50,           -- weight
        'HEALTHY',    -- health_status
        'B1001',      -- father tag (Buck tag)
        'M1001',      -- mother tag (Doe tag)
        70,
        2.5,
        2,
        1);

-- =========================================================================
-- 2) BREEDING RECORDS (5 records, 7-month gap)
-- Farm = 1, Goat Tag = G4001
-- Using expected kidding date for due date
-- =========================================================================
-- NOTE: mate_id is set NULL (if your DB requires it, insert a buck goat and use its goat_id)
INSERT INTO Breeding_Records (breeding_date,
                              pregnancy_status,
                              offspring_count,
                              goat_tag_number,
                              breeder_tag_number,
                              expected_kidding_date,
                              kids_alive,
                              kids_dead,
                              delivery_date,
                              goat_id,
                              mate_id,
                              farm_id)
VALUES
-- OVERDUE (5 days before today)
('2023-12-01', 'PREGNANT', NULL, 'G4002', 'B1002',
DATEADD('DAY', -5, CURRENT_DATE),
NULL, NULL, NULL,
(SELECT goat_id FROM GOAT WHERE tag_number = 'G4002'),
NULL,
1),

-- UPCOMING (2 days from today)
('2023-12-15', 'PREGNANT', NULL, 'G4003', 'B1003',
DATEADD('DAY', 2, CURRENT_DATE),
NULL, NULL, NULL,
(SELECT goat_id FROM GOAT WHERE tag_number = 'G4003'),
NULL,
1),

-- UPCOMING (6 days from today)
('2023-12-18', 'PREGNANT', NULL, 'G4004', 'B1004',
DATEADD('DAY', 6, CURRENT_DATE),
NULL, NULL, NULL,
(SELECT goat_id FROM GOAT WHERE tag_number = 'G4004'),
NULL,
1),

-- FUTURE (15 days from today)
('2023-12-20', 'PREGNANT', NULL, 'G4005', 'B1005',
DATEADD('DAY', 15, CURRENT_DATE),
NULL, NULL, NULL,
(SELECT goat_id FROM GOAT WHERE tag_number = 'G4005'),
NULL,
1);

-- =========================================================================
-- 3) VACCINATION HISTORY (repetition as per your rules)
-- Goal: G4001 (farm_id = 1)
-- =========================================================================

-- ET (dose + booster + 6-month repeats)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2022-06-24', 'Dr. Vet', '2 ml (SC)', 'ET first dose', 'ET_BOOSTER', '2022-07-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET_BOOSTER', '2022-07-15', 'Dr. Vet', '2 ml (SC)', 'ET booster after 21 days', 'ET', '2023-01-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2023-01-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2023-07-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2023-07-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2024-01-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2024-01-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2024-07-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2024-07-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2025-01-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2025-01-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2025-07-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2025-07-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2026-01-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2026-01-15', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2026-07-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'ET', '2026-06-14', 'Dr. Vet', '2 ml (SC)', '6-month repeat', 'ET', '2026-12-14', 1);

-- FMD (dose + booster + 6-month repeats)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2022-08-15', 'Dr. Vet', '2 ml (IM/SC)', 'FMD first dose', 'FMD_BOOSTER', '2022-09-05', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD_BOOSTER', '2022-09-05', 'Dr. Vet', '2 ml (IM/SC)', 'FMD booster after 21 days', 'FMD', '2023-03-05', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2023-03-05', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2023-10-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2023-10-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2024-04-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2024-04-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2024-10-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2024-10-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2025-04-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2025-04-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2025-10-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2025-10-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2026-04-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2026-04-15', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2026-10-15', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'FMD', '2026-06-14', 'Dr. Vet', '2 ml (IM/SC)', '6-month repeat', 'FMD', '2026-12-14', 1);

-- PPR (yearly)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'PPR', '2022-09-10', 'Dr. Vet', '1 ml (SC)', 'PPR yearly', 'PPR', '2023-09-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'PPR', '2023-09-10', 'Dr. Vet', '1 ml (SC)', 'PPR yearly', 'PPR', '2024-09-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'PPR', '2024-09-10', 'Dr. Vet', '1 ml (SC)', 'PPR yearly', 'PPR', '2025-09-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'PPR', '2025-09-10', 'Dr. Vet', '1 ml (SC)', 'PPR yearly', 'PPR', '2026-09-10', 1);

-- HS (yearly, before monsoon)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'HS', '2022-05-10', 'Dr. Vet', '2 ml (SC)', 'Before monsoon', 'HS', '2023-05-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'HS', '2023-05-10', 'Dr. Vet', '2 ml (SC)', 'Before monsoon', 'HS', '2024-05-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'HS', '2024-05-10', 'Dr. Vet', '2 ml (SC)', 'Before monsoon', 'HS', '2025-05-10', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'HS', '2025-05-10', 'Dr. Vet', '2 ml (SC)', 'Before monsoon', 'HS', '2026-05-10', 1);

-- Goat Pox (yearly, pre-winter)
INSERT INTO vaccination_record (goat_id, vaccine_name, vaccination_date, administered_by, dosage, remarks, next_vaccine_name, next_vaccination_date, farm_id)
VALUES
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'GOAT_POX', '2022-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter', 'GOAT_POX', '2023-10-20', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'GOAT_POX', '2023-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter', 'GOAT_POX', '2024-10-20', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'GOAT_POX', '2024-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter', 'GOAT_POX', '2025-10-20', 1),
((SELECT goat_id FROM GOAT WHERE tag_number = 'G4001'), 'GOAT_POX', '2025-10-20', 'Dr. Vet', '0.5 ml (SC)', 'Pre-winter', 'GOAT_POX', '2026-10-20', 1);