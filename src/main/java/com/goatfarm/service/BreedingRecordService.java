package com.goatfarm.service;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Goat;
import com.goatfarm.mapper.BreedingRecordMapper;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.repository.BreedingRecordRepository;
import com.goatfarm.repository.GoatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BreedingRecordService {
    @Autowired private BreedingRecordRepository breedingRecordRepository;
    @Autowired private GoatRepository goatRepository;
    @Autowired private BreedingRecordMapper breedingRecordMapper;

    public BreedingRecordData addBreedingRecord(BreedingRecordData dto) {
        Goat goat = goatRepository.findById(dto.getGoatId())
                .orElseThrow(() -> new EntityNotFoundException("Goat not found"));
        Goat mate = goatRepository.findById(dto.getMateId())
                .orElseThrow(() -> new EntityNotFoundException("Mate not found"));

        if (goat.getFatherTagNumber() != null
                && goat.getFatherTagNumber().equals(mate.getTagNumber())) {
            throw new IllegalArgumentException("Invalid breeding: The mate is the father of the goat.");
        }

        BreedingRecord record = breedingRecordMapper.toEntity(dto, goat, mate);
        BreedingRecord saved = breedingRecordRepository.save(record);
        return breedingRecordMapper.toDto(saved);
    }

    public List<BreedingRecordData> getRecordsByGoatTag(String goatTagNumber) {
        return breedingRecordRepository.findByGoatTagNumber(goatTagNumber)
                .stream()
                .map(breedingRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BreedingRecordData> getRecordsByBreederTag(String breederTagNumber) {
        return breedingRecordRepository.findByBreederTagNumber(breederTagNumber)
                .stream()
                .map(breedingRecordMapper::toDto)
                .collect(Collectors.toList());
    }


    public BreedingRecordData updateBreedingRecord(Long id, BreedingRecordData breedingRecordData) {
        BreedingRecord record = breedingRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Breeding record not found with id: " + id));

        // Apply updates only if fields are non-null
        applyUpdates(record, breedingRecordData);

        BreedingRecord saved = breedingRecordRepository.save(record);
        return breedingRecordMapper.toDto(saved);
    }

    private void applyUpdates(BreedingRecord record, BreedingRecordData breedingRecordData) {
        Optional.ofNullable(breedingRecordData.getBreedingDate()).ifPresent(date -> {
            record.setBreedingDate(date);
            record.setExpected_kidding_date(date.plusDays(150)); // auto-calc
        });

        Optional.ofNullable(breedingRecordData.getPregnancyStatus()).ifPresent(record::setPregnancyStatus);
        Optional.ofNullable(breedingRecordData.getOffspringCount()).ifPresent(record::setOffspringCount);
        Optional.ofNullable(breedingRecordData.getGoatTagNumber()).ifPresent(record::setGoatTagNumber);
        Optional.ofNullable(breedingRecordData.getBreederTagNumber()).ifPresent(record::setBreederTagNumber);

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
}


