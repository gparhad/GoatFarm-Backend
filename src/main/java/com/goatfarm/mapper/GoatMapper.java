package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.GoatData;

import java.util.List;

/** Stateless mapping between {@link Goat} and its DTOs. Client-supplied ids are never copied onto new entities. */
public final class GoatMapper {

    private GoatMapper() {
    }

    public static GoatData toDto(Goat entity) {
        if (entity == null) {
            return null;
        }
        GoatData dto = new GoatData();
        dto.setGoatId(entity.getGoatId());
        dto.setTagNumber(entity.getTagNumber());
        dto.setBreed(entity.getBreed());
        dto.setBirthDate(entity.getBirthDate());
        dto.setWeight(entity.getWeight());
        dto.setHealthStatus(entity.getHealthStatus());
        dto.setFatherTagNumber(entity.getFatherTagNumber());
        dto.setMotherTagNumber(entity.getMotherTagNumber());
        dto.setGender(entity.getGender());
        dto.setHeight(entity.getHeight());
        dto.setMilkPerDay(entity.getMilkPerDay());
        dto.setLastKidCount(entity.getLastKidCount());
        return dto;
    }

    /** Creates a new goat; the id always comes from the database, never from the request. */
    public static Goat toEntity(GoatData dto, Farm farm) {
        if (dto == null) {
            return null;
        }
        Goat entity = new Goat();
        entity.setTagNumber(dto.getTagNumber());
        entity.setBreed(dto.getBreed());
        entity.setBirthDate(dto.getBirthDate());
        entity.setWeight(dto.getWeight());
        entity.setHealthStatus(dto.getHealthStatus());
        entity.setFatherTagNumber(dto.getFatherTagNumber());
        entity.setMotherTagNumber(dto.getMotherTagNumber());
        entity.setGender(dto.getGender());
        entity.setHeight(dto.getHeight());
        entity.setMilkPerDay(dto.getMilkPerDay());
        entity.setLastKidCount(dto.getLastKidCount());
        entity.setFarm(farm);
        return entity;
    }

    /** Full overwrite of the editable fields (PUT semantics, as before). The tag number is never changed. */
    public static void applyUpdate(Goat target, GoatData source) {
        target.setBreed(source.getBreed());
        target.setGender(source.getGender());
        target.setBirthDate(source.getBirthDate());
        target.setWeight(source.getWeight());
        target.setHealthStatus(source.getHealthStatus());
        target.setFatherTagNumber(source.getFatherTagNumber());
        target.setMotherTagNumber(source.getMotherTagNumber());
        target.setHeight(source.getHeight());
        target.setMilkPerDay(source.getMilkPerDay());
        target.setLastKidCount(source.getLastKidCount());
    }

    /** The goat together with its vaccination and breeding records. Needs an open transaction (reads lazy collections). */
    public static GoatData toDtoWithRecords(Goat entity) {
        GoatData dto = toDto(entity);
        if (dto == null) {
            return null;
        }
        dto.setVaccinationRecords(entity.getVaccinationRecords() == null
                ? List.of()
                : entity.getVaccinationRecords().stream().map(VaccinationRecordMapper::toDto).toList());
        dto.setBreedingRecords(entity.getBreedingRecords() == null
                ? List.of()
                : entity.getBreedingRecords().stream().map(BreedingRecordMapper::toDto).toList());
        return dto;
    }
}
