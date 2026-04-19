package com.goatfarm.mapper;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.BreedingRecordData;
import org.springframework.stereotype.Component;

@Component
public class BreedingRecordMapper {

    // Entity → DTO
    public BreedingRecordData toDto(BreedingRecord record) {
        if (record == null) return null;

        BreedingRecordData dto = new BreedingRecordData();
        dto.setBreedingId(record.getBreedingId());
        dto.setBreedingDate(record.getBreedingDate());
        dto.setPregnancyStatus(record.getPregnancyStatus());
        dto.setOffspringCount(record.getOffspringCount());
        dto.setGoatTagNumber(record.getGoatTagNumber());
        dto.setBreederTagNumber(record.getBreederTagNumber());
        dto.setExpectedKiddingDate(record.getExpected_kidding_date());

        if (record.getGoat() != null) {
            dto.setGoatId(record.getGoat().getGoatId());
        }
        if (record.getMate() != null) {
            dto.setMateId(record.getMate().getGoatId());
        }

        return dto;
    }

    // DTO → Entity
    public BreedingRecord toEntity(BreedingRecordData dto, Goat goat, Goat mate) {
        if (dto == null) return null;

        BreedingRecord record = new BreedingRecord();
        record.setBreedingId(dto.getBreedingId());
        record.setBreedingDate(dto.getBreedingDate());
        record.setPregnancyStatus(dto.getPregnancyStatus());
        record.setOffspringCount(dto.getOffspringCount());
        record.setGoatTagNumber(dto.getGoatTagNumber());
        record.setBreederTagNumber(dto.getBreederTagNumber());
        record.setExpected_kidding_date(dto.getExpectedKiddingDate());
        record.setGoat(goat);
        record.setMate(mate);
        if (dto.getBreedingDate() != null) {
            record.setExpected_kidding_date(dto.getBreedingDate().plusDays(150));
        }

        return record;
    }
}
