package com.goatfarm.model;

import java.util.List;

/** JSON view returned by {@code POST /api/farm/addFarm} (same fields the serialised Farm entity had). */
public record FarmRegistrationResponse(Long farmId,
                                       String farmName,
                                       String location,
                                       Double size,
                                       String goatTypes,
                                       List<GoatResponse> goats) {
}
