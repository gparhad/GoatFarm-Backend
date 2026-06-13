package com.goatfarm.mapper;

import com.goatfarm.entity.Goat;
import com.goatfarm.entity.Farm;
import com.goatfarm.model.GoatData;

public class GoatMapper {

    // Convert Entity → DTO
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

    // Convert DTO → Entity
    public static Goat toEntity(GoatData dto, Farm farm) {
        if (dto == null) {
            return null;
        }

        Goat entity = new Goat();
        entity.setGoatId(dto.getGoatId());
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

        // Set relationship
        entity.setFarm(farm);

        return entity;
    }
}
