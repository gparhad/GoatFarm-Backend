package com.goatfarm.mapper;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.BreedingRecordData;
import org.springframework.stereotype.Component;

@Component
public class BreedingRecordMapper {

    // Convert Entity → DTO
    public static BreedingRecordData toDto(BreedingRecord entity) {
        if (entity == null) {
            return null;
        }

        BreedingRecordData dto = new BreedingRecordData();
        dto.setBreedingId(entity.getBreedingId());
        dto.setBreedingDate(entity.getBreedingDate());
        dto.setPregnancyStatus(entity.getPregnancyStatus());
        dto.setOffspringCount(entity.getOffspringCount());
        dto.setGoatTagNumber(entity.getGoatTagNumber());
        dto.setBreederTagNumber(entity.getBreederTagNumber());
        dto.setExpectedKiddingDate(entity.getExpectedKiddingDate());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setKidsAlive(entity.getKidsAlive());
        dto.setKidsDead(entity.getKidsDead());

        // Extract IDs from relationships
        dto.setGoatId(entity.getGoat() != null ? entity.getGoat().getGoatId() : null);
        dto.setMateId(entity.getMate() != null ? entity.getMate().getGoatId() : null);

        return dto;
    }

    // Convert DTO → Entity
    public static BreedingRecord toEntity(BreedingRecordData dto, Goat goat, Goat mate) {
        if (dto == null) {
            return null;
        }

        BreedingRecord entity = new BreedingRecord();
        entity.setBreedingId(dto.getBreedingId());
        entity.setBreedingDate(dto.getBreedingDate());
        entity.setPregnancyStatus(dto.getPregnancyStatus());
        entity.setOffspringCount(dto.getOffspringCount());
        entity.setGoatTagNumber(dto.getGoatTagNumber());
        entity.setBreederTagNumber(dto.getBreederTagNumber());
        entity.setExpectedKiddingDate(dto.getExpectedKiddingDate());
        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setKidsAlive(dto.getKidsAlive());
        entity.setKidsDead(dto.getKidsDead());

        // Set relationships
        entity.setGoat(goat);
        entity.setMate(mate);
        return entity;
    }
}

