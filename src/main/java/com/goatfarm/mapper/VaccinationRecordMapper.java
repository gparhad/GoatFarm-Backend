package com.goatfarm.mapper;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.model.VaccinationRecordData;

public final class VaccinationRecordMapper {

    private VaccinationRecordMapper() {
    }

    public static VaccinationRecordData toDto(VaccinationRecord entity) {
        if (entity == null) {
            return null;
        }
        VaccinationRecordData dto = new VaccinationRecordData();
        dto.setVaccinationId(entity.getVaccinationId());
        dto.setVaccineName(entity.getVaccineName());
        dto.setVaccinationDate(entity.getVaccinationDate());
        dto.setAdministeredBy(entity.getAdministeredBy());
        dto.setDosage(entity.getDosage());
        dto.setRemarks(entity.getRemarks());
        dto.setNextVaccineName(entity.getNextVaccineName());
        dto.setNextVaccinationDate(entity.getNextVaccinationDate());
        dto.setGoatId(entity.getGoat() != null ? entity.getGoat().getGoatId() : null);
        dto.setGoatTagNumber(entity.getGoat() != null ? entity.getGoat().getTagNumber() : null);
        dto.setFarmId(entity.getFarmId());
        return dto;
    }

    /** Creates a new record; the id always comes from the database, never from the request. */
    public static VaccinationRecord toEntity(VaccinationRecordData dto, Goat goat) {
        if (dto == null) {
            return null;
        }
        VaccinationRecord entity = new VaccinationRecord();
        entity.setVaccineName(dto.getVaccineName());
        entity.setVaccinationDate(dto.getVaccinationDate());
        entity.setAdministeredBy(dto.getAdministeredBy());
        entity.setDosage(dto.getDosage());
        entity.setRemarks(dto.getRemarks());
        entity.setNextVaccineName(dto.getNextVaccineName());
        entity.setNextVaccinationDate(dto.getNextVaccinationDate());
        entity.setGoat(goat);
        if (goat != null && goat.getFarm() != null) {
            entity.setFarmId(goat.getFarm().getFarmId());
        }
        return entity;
    }
}
