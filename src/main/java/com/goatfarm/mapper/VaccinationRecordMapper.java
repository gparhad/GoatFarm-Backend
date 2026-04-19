package com.goatfarm.mapper;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.model.VaccinationRecordData;
import org.springframework.stereotype.Component;

@Component
public class VaccinationRecordMapper {

    public VaccinationRecordData toDto(VaccinationRecord record) {
        if (record == null) return null;

        VaccinationRecordData dto = new VaccinationRecordData();
        dto.setVaccinationId(record.getVaccinationId());
        dto.setGoatId(record.getGoat().getGoatId());
        dto.setGoatTagNumber(record.getGoat().getTagNumber());
        dto.setVaccineName(record.getVaccineName());
        dto.setVaccinationDate(record.getVaccinationDate());
        dto.setAdministeredBy(record.getAdministeredBy());
        dto.setDosage(record.getDosage());
        dto.setRemarks(record.getRemarks());
        dto.setNextVaccinationDate(record.getNextVaccinationDate());
        dto.setNextVaccineName(record.getNextVaccineName());
        return dto;
    }

    public VaccinationRecord toEntity(VaccinationRecordData dto, Goat goat) {
        if (dto == null) return null;

        VaccinationRecord record = new VaccinationRecord();
        record.setVaccinationId(dto.getVaccinationId());
        record.setGoat(goat);
        record.setVaccineName(dto.getVaccineName());
        record.setVaccinationDate(dto.getVaccinationDate());
        record.setAdministeredBy(dto.getAdministeredBy());
        record.setDosage(dto.getDosage());
        record.setRemarks(dto.getRemarks());
        return record;
    }
}

