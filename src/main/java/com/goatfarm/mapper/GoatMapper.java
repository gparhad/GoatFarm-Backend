package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.model.GoatData;


public class GoatMapper {

    // Entity → DTO
    public static GoatData toDto(Goat goat) {
        if (goat == null) return null;

        GoatData dto = new GoatData();
        dto.setGoatId(goat.getGoatId());
        dto.setTagNumber(goat.getTagNumber());
        dto.setBreed(goat.getBreed());
        dto.setBirthDate(goat.getBirthDate());
        dto.setWeight(goat.getWeight());
        dto.setHealthStatus(goat.getHealthStatus());
        dto.setFatherTagNumber(goat.getFatherTagNumber());
        dto.setMotherTagNumber(goat.getMotherTagNumber());
        dto.setGender(goat.getGender());
        return dto;
    }

    // DTO → Entity
    public static Goat toEntity(GoatData dto, Farm farm) {
        if (dto == null) return null;

        Goat goat = new Goat();
        goat.setGoatId(dto.getGoatId());
        goat.setTagNumber(dto.getTagNumber());
        goat.setBreed(dto.getBreed());
        goat.setBirthDate(dto.getBirthDate());
        goat.setWeight(dto.getWeight());
        goat.setHealthStatus(dto.getHealthStatus());
        goat.setFarm(farm); // attach farm entity from DB
        goat.setFatherTagNumber(dto.getFatherTagNumber());
        goat.setMotherTagNumber(dto.getMotherTagNumber());
        goat.setGender(dto.getGender());

        return goat;
    }
}


