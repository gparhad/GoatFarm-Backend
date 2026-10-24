package com.goatfarm.mapper;

import com.goatfarm.entity.Farm;
import com.goatfarm.model.FarmData;

public final class FarmMapper {

    private FarmMapper() {
    }

    /** Must run inside a transaction (reads the lazy farmer). */
    public static FarmData toDto(Farm farm) {
        if (farm == null) {
            return null;
        }
        FarmData dto = new FarmData();
        dto.setFarmId(farm.getFarmId());
        dto.setFarmName(farm.getFarmName());
        dto.setLocation(farm.getLocation());
        dto.setSize(farm.getSize());
        dto.setGoatTypes(farm.getGoatTypes());
        if (farm.getFarmer() != null) {
            dto.setUserId(farm.getFarmer().getUserId());
            dto.setFarmer(UserMapper.toDto(farm.getFarmer(),false));
        }
        return dto;
    }

    /** New farm from a registration request; the id and the farmer are set by the service, never taken from the client. */
    public static Farm toEntity(FarmData dto) {
        if (dto == null) {
            return null;
        }
        Farm farm = new Farm();
        farm.setFarmName(dto.getFarmName());
        farm.setLocation(dto.getLocation());
        farm.setSize(dto.getSize());
        farm.setGoatTypes(dto.getGoatTypes());
        return farm;
    }
}
