package com.goatfarm.service;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.BreedingCheckResult;
import com.goatfarm.mapper.BreedingRecordMapper;
import com.goatfarm.model.AuthUser;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.model.GoatTreeNode;
import com.goatfarm.model.UpcomingDeliveryDateData;
import com.goatfarm.repository.BreedingRecordRepository;
import com.goatfarm.repository.GoatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BreedingRecordService {

    private final BreedingRecordRepository breedingRecordRepository;
    private final GoatRepository goatRepository;
    private final BreedingRecordMapper breedingRecordMapper;

    private static final int MAX_GENERATION_DEPTH = 3;

    public BreedingRecordService(
            BreedingRecordRepository breedingRecordRepository,
            GoatRepository goatRepository,
            BreedingRecordMapper breedingRecordMapper
    ) {
        this.breedingRecordRepository = breedingRecordRepository;
        this.goatRepository = goatRepository;
        this.breedingRecordMapper = breedingRecordMapper;
    }

    public BreedingRecordData addBreedingRecord(BreedingRecordData dto, Long farmId) {
        if (breedingRecordRepository.existsByGoatTagNumberAndPregnancyStatusAndFarm_FarmId(
                dto.getGoatTagNumber(), "PREGNANT", farmId)) {
            throw new IllegalArgumentException("Goat already has an active pregnancy. Cannot add new breeding record.");
        }

        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(dto.getGoatTagNumber(), farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found"));
        Goat breeder = goatRepository.findByTagNumberAndFarm_FarmId(dto.getBreederTagNumber(), farmId)
                .orElseThrow(() -> new EntityNotFoundException("Breeder not found"));

        BreedingCheckResult result = evaluateRelation(goat, breeder, farmId);
        if (result.isInbreeding()) {
            throw new IllegalArgumentException(result.getMessage());
        }

        BreedingRecord record = breedingRecordMapper.toEntity(dto, goat, breeder);
        BreedingRecord saved = breedingRecordRepository.save(record);

        return breedingRecordMapper.toDto(saved);
    }

    public List<BreedingRecordData> getRecordsByGoatTag(String goatTagNumber, Long farmId) {
        return breedingRecordRepository.findByGoatTagNumberAndFarm_FarmId(goatTagNumber, farmId)
                .stream()
                .map(BreedingRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BreedingRecordData> getRecordsByBreederTag(String breederTagNumber, Long farmId) {
        return breedingRecordRepository.findByBreederTagNumberAndFarm_FarmId(breederTagNumber, farmId)
                .stream()
                .map(BreedingRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public BreedingRecordData updateBreedingRecord(Long id, BreedingRecordData breedingRecordData, Long farmId) {
        BreedingRecord record = breedingRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Breeding record not found with id: " + id));

        applyUpdates(record, breedingRecordData);

        BreedingRecord saved = breedingRecordRepository.save(record);
        return breedingRecordMapper.toDto(saved);
    }

    private void applyUpdates(BreedingRecord record, BreedingRecordData breedingRecordData) {
        Optional.ofNullable(breedingRecordData.getBreedingDate()).ifPresent(localDate -> {
            record.setBreedingDate(localDate);
            record.setExpectedKiddingDate(localDate.plusDays(150)); // auto-calc
        });

        Optional.ofNullable(breedingRecordData.getPregnancyStatus()).ifPresent(record::setPregnancyStatus);
        Optional.ofNullable(breedingRecordData.getOffspringCount()).ifPresent(record::setOffspringCount);
        Optional.ofNullable(breedingRecordData.getKidsAlive()).ifPresent(record::setKidsAlive);
        Optional.ofNullable(breedingRecordData.getKidsDead()).ifPresent(record::setKidsDead);

        if (breedingRecordData.getGoatId() != null) {
            Goat goat = goatRepository.findById(breedingRecordData.getGoatId())
                    .orElseThrow(() -> new EntityNotFoundException("Goat not found"));
            record.setGoat(goat);
        }
        if (breedingRecordData.getMateId() != null) {
            Goat mate = goatRepository.findById(breedingRecordData.getMateId())
                    .orElseThrow(() -> new EntityNotFoundException("Mate not found"));
            record.setMate(mate);
        }
    }

    public Map<String, Object> checkInbreeding(String goatTag, String breederTag, Long farmId) {
        Goat goat = getGoat(goatTag, farmId);
        Goat breeder = getGoat(breederTag, farmId);

        BreedingCheckResult result = evaluateRelation(goat, breeder, farmId);

        Map<String, Object> response = new HashMap<>();
        response.put("isInbreeding", result.isInbreeding());
        response.put("message", result.getMessage());
        response.put("commonAncestors", result.getCommonAncestors());

        response.put("goatTree", buildTree(goatTag, MAX_GENERATION_DEPTH, farmId));
        response.put("breederTree", buildTree(breederTag, MAX_GENERATION_DEPTH, farmId));

        response.put("safeBreeders", result.isInbreeding() ? findSafeBreeders(goat, farmId) : List.of());

        return response;
    }

    private BreedingCheckResult evaluateRelation(Goat goat, Goat breeder, Long farmId) {
        Set<String> goatAncestors = getAncestors(goat.getTagNumber(), MAX_GENERATION_DEPTH, farmId);
        Set<String> breederAncestors = getAncestors(breeder.getTagNumber(), MAX_GENERATION_DEPTH, farmId);

        Set<String> common = new HashSet<>(goatAncestors);
        common.retainAll(breederAncestors);

        boolean direct = isDirectRelation(goat, breeder, common, farmId);
        boolean sibling = isSiblingRelation(goat, breeder, common, farmId);
        boolean cousin = isCousinRelation(goat, breeder, common, farmId);

        boolean inbreeding = direct || sibling || cousin || !common.isEmpty();

        String message = resolveMessage(direct, sibling, cousin, common);

        return new BreedingCheckResult(inbreeding, message, common);
    }

    private boolean isDirectRelation(Goat goat, Goat breeder, Set<String> common, Long farmId) {
        if (Objects.equals(goat.getFatherTagNumber(), breeder.getTagNumber()) ||
                Objects.equals(goat.getMotherTagNumber(), breeder.getTagNumber())) {
            common.add(breeder.getTagNumber());
            return true;
        }
        return false;
    }

    private boolean isSiblingRelation(Goat goat, Goat breeder, Set<String> common, Long farmId) {
        Set<String> goatParents = getParents(goat.getTagNumber(), farmId);
        Set<String> breederParents = getParents(breeder.getTagNumber(), farmId);

        Set<String> overlap = new HashSet<>(goatParents);
        overlap.retainAll(breederParents);

        if (!overlap.isEmpty()) {
            common.addAll(overlap);
            return true;
        }
        return false;
    }

    private boolean isCousinRelation(Goat goat, Goat breeder, Set<String> common, Long farmId) {
        Set<String> goatGrandparents = getGrandparents(goat.getTagNumber(), farmId);
        Set<String> breederGrandparents = getGrandparents(breeder.getTagNumber(), farmId);

        Set<String> overlap = new HashSet<>(goatGrandparents);
        overlap.retainAll(breederGrandparents);

        if (!overlap.isEmpty()) {
            common.addAll(overlap);
            return true;
        }
        return false;
    }

    // Java 17 compatible (no switch on boolean)
    private String resolveMessage(boolean direct, boolean sibling, boolean cousin, Set<String> common) {
        if (direct) {
            return "Parent-child relation detected!";
        } else if (sibling) {
            return "Sibling inbreeding detected!";
        } else if (cousin) {
            return "A Cousin relation detected (avoid breeding)!";
        } else if (!common.isEmpty()) {
            return "Common ancestor found: " + common;
        } else {
            return "Safe for breeding!";
        }
    }

    private List<String> findSafeBreeders(Goat goat, Long farmId) {
        return goatRepository.findByGenderAndFarm_FarmId("MALE",farmId).stream()
                .filter(b -> !b.getTagNumber().equals(goat.getTagNumber()))
                .filter(b -> !evaluateRelation(goat, b, farmId).isInbreeding())
                .map(Goat::getTagNumber)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoatTreeNode buildTree(String tag, int depth, Long farmId) {
        return buildTreeInternal(tag, depth, new HashSet<>(), farmId);
    }

    private GoatTreeNode buildTreeInternal(String tag, int depth, Set<String> visited, Long farmId) {
        if (tag == null || depth <= 0 || visited.contains(tag)) {
            return null;
        }

        visited.add(tag);

        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tag, farmId).orElse(null);
        if (goat == null) return null;

        GoatTreeNode node = new GoatTreeNode();
        node.setTagNumber(tag);
        node.setFather(buildTreeInternal(goat.getFatherTagNumber(), depth - 1, visited, farmId));
        node.setMother(buildTreeInternal(goat.getMotherTagNumber(), depth - 1, visited, farmId));

        return node;
    }

    private Goat getGoat(String tag, Long farmId) {
        return goatRepository.findByTagNumberAndFarm_FarmId(tag, farmId)
                .orElseThrow(() -> new EntityNotFoundException("Goat not found: " + tag));
    }

    private Set<String> getAncestors(String tag, int depth, Long farmId) {
        if (tag == null || depth <= 0) return Set.of();

        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tag, farmId).orElse(null);
        if (goat == null) return Set.of();

        Set<String> ancestors = new HashSet<>();
        if (goat.getFatherTagNumber() != null) {
            ancestors.add(goat.getFatherTagNumber());
            ancestors.addAll(getAncestors(goat.getFatherTagNumber(), depth - 1, farmId));
        }
        if (goat.getMotherTagNumber() != null) {
            ancestors.add(goat.getMotherTagNumber());
            ancestors.addAll(getAncestors(goat.getMotherTagNumber(), depth - 1, farmId));
        }
        return ancestors;
    }

    private Set<String> getParents(String tag, Long farmId) {
        Goat goat = goatRepository.findByTagNumberAndFarm_FarmId(tag, farmId).orElse(null);
        if (goat == null) return Set.of();

        return Stream.of(goat.getFatherTagNumber(), goat.getMotherTagNumber())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> getGrandparents(String tag, Long farmId) {
        return getParents(tag, farmId).stream()
                .flatMap(parentTag -> getParents(parentTag, farmId).stream())
                .collect(Collectors.toSet());
    }

    public Map<String, Object> getLatestPregnantRecord(String goatTag, Long farmId) {
        var recordOpt = breedingRecordRepository.findByGoatTagNumberAndPregnancyStatusAndFarm_FarmIdOrderByBreedingDateDesc(
                goatTag, "PREGNANT", farmId);

        if (recordOpt.isEmpty()) {
            throw new EntityNotFoundException("No active pregnancy found for the goat");
        }

        BreedingRecord record = recordOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Active breeding record found");
        response.put("data", breedingRecordMapper.toDto(record));
        return response;
    }

    @Transactional
    public Map<String, Object> updateDeliveryDetails(String goatTag, BreedingRecordData dto, Long farmId) {
        var recordOpt = breedingRecordRepository.findByGoatTagNumberAndPregnancyStatusAndFarm_FarmIdOrderByBreedingDateDesc(
                goatTag, "PREGNANT", farmId);

        if (recordOpt.isEmpty()) {
            throw new EntityNotFoundException("No active pregnancy found for the goat");
        }

        BreedingRecord record = recordOpt.get();

        // VALIDATION
        if ("DELIVERED".equals(dto.getPregnancyStatus())) {
            if (dto.getDeliveryDate() == null) throw new IllegalArgumentException("Delivery date is required");
            if (dto.getOffspringCount() == null) throw new IllegalArgumentException("Offspring count is required");
            if (dto.getKidsAlive() == null || dto.getKidsDead() == null) throw new IllegalArgumentException("Kids alive/dead required");
            if (dto.getOffspringCount() != (dto.getKidsAlive() + dto.getKidsDead())) throw new IllegalArgumentException("Kids alive + dead must equal offspring count");
        }

        if ("ABORTED".equals(dto.getPregnancyStatus())) {
            record.setOffspringCount(0);
            record.setKidsAlive(0);
            record.setKidsDead(0);
            record.setDeliveryDate(dto.getDeliveryDate());
        }

        // UPDATE
        record.setPregnancyStatus(dto.getPregnancyStatus());
        record.setDeliveryDate(dto.getDeliveryDate());
        record.setOffspringCount(dto.getOffspringCount());
        record.setKidsAlive(dto.getKidsAlive());
        record.setKidsDead(dto.getKidsDead());

        BreedingRecord saved = breedingRecordRepository.save(record);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Breeding record updated successfully");
        response.put("data", breedingRecordMapper.toDto(saved));

        return response;
    }

    public List<UpcomingDeliveryDateData> getUpcomingDeliveriesWindow(Long farmId, int pastDays, int futureDays) {
        if (farmId == null) throw new IllegalArgumentException("FarmId must not be null");
        if (pastDays < 0) throw new IllegalArgumentException("pastDays must be >= 0");
        if (futureDays < 0) throw new IllegalArgumentException("futureDays must be >= 0");

        LocalDate from = LocalDate.now().minusDays(pastDays);
        LocalDate to = LocalDate.now().plusDays(futureDays);

        var records = breedingRecordRepository.findDueDeliveriesInWindow(farmId, from, to, "PREGNANT");

        return records.stream()
                .map(b -> new UpcomingDeliveryDateData(
                        b.getBreedingId(),
                        b.getBreedingDate(),
                        b.getPregnancyStatus(),
                        b.getOffspringCount(),
                        b.getGoatTagNumber(),
                        b.getBreederTagNumber(),
                        b.getExpectedKiddingDate(),
                        b.getDeliveryDate(),
                        b.getKidsAlive(),
                        b.getKidsDead()

                ))
                .collect(Collectors.toList());
    }

    public List<UpcomingDeliveryDateData> getUpcomingDeliveriesWindowDefault(Long farmId) {
        return getUpcomingDeliveriesWindow(farmId, 3, 7);
    }
}