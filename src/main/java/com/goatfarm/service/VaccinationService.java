package com.goatfarm.service;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.mapper.VaccinationRecordMapper;
import com.goatfarm.model.VaccinationRecordData;
import com.goatfarm.repository.GoatRepository;
import com.goatfarm.repository.VaccinationRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VaccinationService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final GoatRepository goatRepository;
    private final VaccinationRecordMapper vaccinationRecordMapper;

    public VaccinationService(
            VaccinationRecordRepository vaccinationRecordRepository,
            GoatRepository goatRepository,
            VaccinationRecordMapper vaccinationRecordMapper
    ) {
        this.vaccinationRecordRepository = vaccinationRecordRepository;
        this.goatRepository = goatRepository;
        this.vaccinationRecordMapper = vaccinationRecordMapper;
    }

    /**
     * Add vaccination record for goatTagNumber within given farm.
     * No functional change in validation rules.
     * Adds farm-scope security.
     */
    public VaccinationRecordData addVaccinationRecord(VaccinationRecordData dto, Long farmId) {
        if (dto == null || dto.getGoatTagNumber() == null || dto.getGoatTagNumber().isBlank()) {
            throw new IllegalArgumentException("Goat tag number is required");
        }
        if (dto.getVaccineName() == null || dto.getVaccineName().isBlank()) {
            throw new IllegalArgumentException("Vaccine name is required");
        }
        if (dto.getVaccinationDate() == null) {
            throw new IllegalArgumentException("Vaccination date is required");
        }

        // farm scoped goat fetch
        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(dto.getGoatTagNumber(), farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + dto.getGoatTagNumber()));

        List<VaccinationRecord> records = vaccinationRecordRepository.findByGoat(goat);
        if (!records.isEmpty()) {
            // Rule 1: 21-day gap check
            checkIntervalBetweenTwoVaccines(dto, records);

            // Rule 2: Booster enforcement
            validateVaccination(records, dto);

            // Rule 3: Post-booster minimum interval check
            // validateVaccinationOrder(records, dto);
        }

        VaccinationRecord record = vaccinationRecordMapper.toEntity(dto, goat);
        applySchedule(record, goat);

        VaccinationRecord saved = vaccinationRecordRepository.save(record);
        return vaccinationRecordMapper.toDto(saved);
    }

    /**
     * Vaccination history by goat tag within farm.
     * No UI change: same endpoint behavior, but farm protected now.
     */
    public List<VaccinationRecordData> getVaccinationHistoryByTagNumber(String tagNumber, Long farmId) {
        if (tagNumber == null || tagNumber.isBlank()) {
            throw new IllegalArgumentException("Tag number is required");
        }

        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tagNumber, farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found with tag: " + tagNumber));

        List<VaccinationRecord> records = vaccinationRecordRepository.findByGoat(goat);

        return records.stream()
                .sorted(Comparator.comparing(VaccinationRecord::getVaccinationDate))
                .map(VaccinationRecordMapper::toDto)
                .toList();
    }

    /**
     * Upcoming vaccinations due within N days for a farm.
     * No functional change: returns empty list for invalid input.
     */
    public List<VaccinationRecordData> getUpcomingVaccinations(Long farmId, int days) {
        if (farmId == null || days <= 0) {
            return List.of();
        }

        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days); // Inclusive window

        return vaccinationRecordRepository.findUpcomingVaccinations(farmId, from, to).stream()
                .map(VaccinationRecordMapper::toDto)
                .toList();
    }

    // Existing logic (your method name says tomorrow but you used now+7).
    // Keep as-is (no functional change).
    public List<Map<String, Object>> getTomorrowVaccinationsByFarm2(Long farmId) {
        LocalDate tomorrow = LocalDate.now().plusDays(7);
        List<VaccinationRecord> records = vaccinationRecordRepository.findDueVaccinationsByFarmAndDate(farmId, tomorrow);

        return records.stream()
                .map(record -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("goatTagNumber", record.getGoat().getTagNumber());
                    entry.put("vaccineName", record.getNextVaccineName());
                    entry.put("lastDate", record.getVaccinationDate());
                    entry.put("nextVaccinationDate", record.getNextVaccinationDate());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // VALIDATION RULES (kept same behavior)
    private static void checkIntervalBetweenTwoVaccines(VaccinationRecordData dto, List<VaccinationRecord> records) {
        Optional<VaccinationRecord> lastRecordOpt = records.stream()
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate));

        if (lastRecordOpt.isPresent()) {
            VaccinationRecord lastRecord = lastRecordOpt.get();
            if (dto.getVaccinationDate().isBefore(lastRecord.getVaccinationDate().plusDays(21))) {
                throw new IllegalArgumentException(
                        "Invalid vaccination: No two vaccines/deworming should be given within 21 days. " +
                                "Last record was on " + lastRecord.getVaccinationDate());
            }
        }
    }

    // Rule 2: Booster enforcement (kept same logic)
    private void validateVaccination(List<VaccinationRecord> records, VaccinationRecordData dto) {
        Optional<VaccinationRecord> lastRecordOpt = records.stream()
                .filter(r -> r.getVaccineName().equalsIgnoreCase(dto.getVaccineName()))
                .max(Comparator.comparing(VaccinationRecord::getVaccinationDate));

        if (lastRecordOpt.isPresent()) {
            VaccinationRecord lastRecord = lastRecordOpt.get();
            boolean lastWasBooster = isBooster(lastRecord);
            boolean currentIsBooster = isBooster(dto);

            if (!lastWasBooster && requiresBooster(lastRecord.getVaccineName())) {
                if (!currentIsBooster || !matchesVaccineType(lastRecord, dto.getVaccineName())) {
                    throw new IllegalArgumentException(
                            "Invalid vaccination: After " + lastRecord.getVaccineName() +
                                    " only its booster is allowed next.");
                }
            }

            if (lastWasBooster) {
                enforceMinIntervalAfterBooster(lastRecord, dto);
            }
        }
    }

    private void enforceMinIntervalAfterBooster(VaccinationRecord lastRecord, VaccinationRecordData dto) {
        String vaccine = dto.getVaccineName();
        LocalDate date = dto.getVaccinationDate();

        if (matchesVaccineType(lastRecord, "FMD")) {
            if (date.isBefore(lastRecord.getVaccinationDate().plusMonths(6))) {
                throw new IllegalArgumentException("Invalid vaccination: After FMD booster, next FMD dose cannot be given before 6 months.");
            }
        }

        if (matchesVaccineType(lastRecord, "Goat Pox") ||
                matchesVaccineType(lastRecord, "Enterotoxemia") ||
                matchesVaccineType(lastRecord, "Hemorrhagic Septicemia")) {
            if (date.isBefore(lastRecord.getVaccinationDate().plusYears(1))) {
                throw new IllegalArgumentException("Invalid vaccination: After " + vaccine + " booster, next dose cannot be given before 12 months.");
            }
        }
    }

    private boolean matchesVaccineType(VaccinationRecord record, String vaccineName) {
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
                || vaccineName.equals("Enterotoxemia")
                || vaccineName.equals("Hemorrhagic Septicemia");
    }

    // Scheduling Logic (unchanged)
    private void applySchedule(VaccinationRecord record, Goat goat) {
        LocalDate date = record.getVaccinationDate();

        switch (record.getVaccineName()) {
            case "PPR":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(3));
                break;
            case "FMD":
                record.setNextVaccineName("FMD Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;
            case "FMD Booster":
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
            case "Enterotoxemia":
                record.setNextVaccineName("Enterotoxemia Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;
            case "Enterotoxemia Booster":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(1));
                break;
            case "Hemorrhagic Septicemia":
                record.setNextVaccineName("Hemorrhagic Septicemia Booster");
                record.setNextVaccinationDate(date.plusWeeks(3));
                break;
            case "Hemorrhagic Septicemia Booster":
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusYears(1));
                break;
            case "Deworming":
                if (goat.getBirthDate() != null && goat.getBirthDate().plusMonths(3).isAfter(date)) {
                    throw new IllegalArgumentException("Deworming cannot be given before 3 months of age.");
                }
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusMonths(3));
                break;
            default:
                record.setNextVaccineName("Not Defined");
                record.setNextVaccinationDate(date.plusMonths(3));
                break;
        }
    }
}