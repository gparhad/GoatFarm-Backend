package com.goatfarm.service;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.mapper.VaccinationRecordMapper;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.repository.GoatRepository;
import com.goatfarm.repository.VaccinationRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VaccinationService {
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    @Autowired
    private GoatRepository goatRepository;
    @Autowired
    private VaccinationRecordMapper vaccinationRecordMapper;

    public VaccinationRecordData addVaccinationRecord(VaccinationRecordData dto) {
        Goat goat = goatRepository.findByTagNumber(dto.getGoatTagNumber())
                .orElseThrow(() -> new EntityNotFoundException("Goat not found"));

        List<VaccinationRecord> records = vaccinationRecordRepository.findByGoat(goat);
        if (!records.isEmpty()) {
            // ✅ Rule 1: 21-day gap check
            checkIntervalBetweenTwoVaccines(dto, records);

            // ✅ Rule 2: Booster enforcement
            validateVaccination(records, dto);

            // ✅ Rule 3: Post-booster minimum interval check
            validateVaccinationOrder(records, dto);
        }


        VaccinationRecord record = vaccinationRecordMapper.toEntity(dto, goat);
        applySchedule(record,goat);
        VaccinationRecord saved = vaccinationRecordRepository.save(record);
        return vaccinationRecordMapper.toDto(saved);
    }

    private static void checkIntervalBetweenTwoVaccines(VaccinationRecordData dto, List<VaccinationRecord> records) {
        VaccinationRecord lastRecord = records.stream()
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate))
                .orElse(null);

        if (lastRecord != null &&
                dto.getVaccinationDate().isBefore(lastRecord.getVaccinationDate().plusDays(21))) {
            throw new IllegalArgumentException(
                    "Invalid vaccination: No two vaccines/deworming should be given within 21 days. " +
                            "Last record was on " + lastRecord.getVaccinationDate()
            );
        }
    }

    private void validateVaccinationOrder(List<VaccinationRecord> records, VaccinationRecordData dto) {
        String vaccine = dto.getVaccineName();

        // Find the last vaccination record
        VaccinationRecord lastRecord = records.stream()
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate))
                .orElse(null);

        if (lastRecord == null) return; // First vaccination, always allowed

        boolean lastWasBooster = isBooster(lastRecord);
        boolean currentIsBooster = isBooster(dto);

        // Case 1: Last was a normal dose that requires booster
        if (!lastWasBooster && requiresBooster(lastRecord.getVaccineName())) {
            // Only allow the booster of the same vaccine
            if (!currentIsBooster || matchesVaccineType(lastRecord, vaccine)) {
                throw new IllegalArgumentException(
                        "Invalid vaccination: After " + lastRecord.getVaccineName() +
                                " only its booster is allowed next."
                );
            }
        }

        // Case 2: Last was a booster
        if (lastWasBooster) {
            // Enforce minimum interval rules before next dose of same vaccine
            if (matchesVaccineType(lastRecord, vaccine)) {
                if (vaccine.equals("FMD") &&
                        dto.getVaccinationDate().isBefore(lastRecord.getVaccinationDate().plusMonths(6))) {
                    throw new IllegalArgumentException(
                            "Invalid vaccination: After FMD booster, next FMD dose cannot be given before 6 months."
                    );
                }
                if ((vaccine.equals("Goat Pox") || vaccine.equals("Enterotoxaemia") || vaccine.equals("Hemorrhagic Septicaemia")) &&
                        dto.getVaccinationDate().isBefore(lastRecord.getVaccinationDate().plusYears(1))) {
                    throw new IllegalArgumentException(
                            "Invalid vaccination: After " + vaccine + " booster, next dose cannot be given before 12 months."
                    );
                }
            }
            // If different vaccine → allowed (order can be anything after booster)
        }
    }

    private void validateVaccination(List<VaccinationRecord> records, VaccinationRecordData dto) {
        String vaccine = dto.getVaccineName();
        LocalDate date = dto.getVaccinationDate();

        // Find the last vaccination record
        VaccinationRecord lastRecord = records.stream()
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate))
                .orElse(null);

        if (lastRecord == null) return; // First vaccination, always allowed

        boolean lastWasBooster = isBooster(lastRecord);
        boolean currentIsBooster = isBooster(dto);

        // Case 1: Last was a normal dose that requires booster
        if (!lastWasBooster && requiresBooster(lastRecord.getVaccineName())) {
            // Only allow the booster of the same vaccine
            if (!currentIsBooster || matchesVaccineType(lastRecord, vaccine)) {
                throw new IllegalArgumentException(
                        "Invalid vaccination: After " + lastRecord.getVaccineName() +
                                " only its booster is allowed next."
                );
            }
        }

        // Case 2: Last was a booster
        if (lastWasBooster) {
            // If same vaccine type, enforce minimum interval
            if (matchesVaccineType(lastRecord, vaccine)) {
                if (vaccine.equals("FMD") &&
                        date.isBefore(lastRecord.getVaccinationDate().plusMonths(6))) {
                    throw new IllegalArgumentException(
                            "Invalid vaccination: After FMD booster, next FMD dose cannot be given before 6 months."
                    );
                }
                if ((vaccine.equals("Goat Pox") || vaccine.equals("Enterotoxaemia") || vaccine.equals("Hemorrhagic Septicaemia")) &&
                        date.isBefore(lastRecord.getVaccinationDate().plusYears(1))) {
                    throw new IllegalArgumentException(
                            "Invalid vaccination: After " + vaccine + " booster, next dose cannot be given before 12 months."
                    );
                }
            }
            // If different vaccine → allowed (order can be anything after booster)
        }
    }


    private boolean matchesVaccineType(VaccinationRecord record, String vaccineName) {
        // "FMD Booster" matches "FMD", "Goat Pox Booster" matches "Goat Pox"
        return record.getVaccineName().toLowerCase().startsWith(vaccineName.toLowerCase());
    }

    private boolean isBooster(VaccinationRecord record) {
        return record.getVaccineName().toLowerCase().contains("booster");
    }

    private boolean isBooster(VaccinationRecordData dto) {
        return dto.getVaccineName().toLowerCase().contains("booster");
    }


    private boolean requiresBooster(String vaccineName) {
        return vaccineName.equals("FMD")
                || vaccineName.equals("Goat Pox")
                || vaccineName.equals("Enterotoxaemia")
                || vaccineName.equals("Hemorrhagic Septicaemia");
    }



    public List<VaccinationRecordData> getVaccinationHistoryByTag(String tagNumber) {
        Goat goat = goatRepository.findByTagNumber(tagNumber)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));

        List<VaccinationRecord> records = vaccinationRecordRepository.findByGoat(goat);

        return records.stream()
                .sorted(Comparator.comparing(VaccinationRecord::getVaccinationDate))
                .map(vaccinationRecordMapper::toDto)
                .collect(Collectors.toList());
    }
    private void applySchedule(VaccinationRecord record, Goat goat) {
        String name = record.getVaccineName();
        LocalDate date = record.getVaccinationDate();

        switch (name) {
            case "PPR":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(3));
                break;

            case "FMD":
                // Normal dose → booster due after 3 weeks
                record.setNextVaccineName("FMD Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;

            case "FMD Booster":
                // Booster → next FMD dose only after 6 months
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusMonths(6));
                break;

            case "Goat Pox":
                record.setNextVaccineName("Goat Pox Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;

            case "Goat Pox Booster":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(1));
                break;

            case "Enterotoxaemia":
                record.setNextVaccineName("Enterotoxaemia Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;

            case "Enterotoxaemia Booster":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(1));
                break;

            case "Hemorrhagic Septicaemia":
                record.setNextVaccineName("Hemorrhagic Septicaemia Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;

            case "Hemorrhagic Septicaemia Booster":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(1));
                break;

            case "Deworming":
                if (goat.getBirthDate() != null &&
                        goat.getBirthDate().plusMonths(3).isAfter(date)) {
                    throw new IllegalArgumentException("Deworming cannot be given before 3 months of age.");
                }
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusMonths(3));
                break;

            default:
                record.setNextVaccineName(null);
                record.setNextVaccinationDate(null);
        }
    }

    public List<Map<String, Object>> getTomorrowVaccinationsByFarm(Long farmId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Find all goats belonging to this farm
        List<Goat> goats = goatRepository.findByFarmFarmId(farmId);

        List<Map<String, Object>> schedules = new ArrayList<>();

        for (Goat goat : goats) {
            List<VaccinationRecord> records = vaccinationRecordRepository.findByGoat(goat);

            for (VaccinationRecord record : records) {
                if (record.getNextVaccinationDate() != null &&
                        record.getNextVaccinationDate().isEqual(tomorrow)) {

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("goatTagNumber", goat.getTagNumber());
                    entry.put("lastVaccine", record.getVaccineName());
                    entry.put("lastDate", record.getVaccinationDate());
                    entry.put("nextVaccineName", record.getNextVaccineName());
                    entry.put("nextVaccineDate", record.getNextVaccinationDate());

                    schedules.add(entry);
                }
            }
        }

        return schedules;
    }

    public List<Map<String, Object>> getTomorrowVaccinationsByFarm2(Long farmId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<VaccinationRecord> records = vaccinationRecordRepository.findDueVaccinationsByFarmAndDate(farmId, tomorrow);

        return records.stream()
                .map(record -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("goatTagNumber", record.getGoat().getTagNumber());
                    entry.put("lastVaccine", record.getVaccineName());
                    entry.put("lastDate", record.getVaccinationDate());
                    entry.put("nextVaccineName", record.getNextVaccineName());
                    entry.put("nextVaccineDate", record.getNextVaccinationDate());
                    return entry;
                })
                .collect(Collectors.toList());
    }



}

