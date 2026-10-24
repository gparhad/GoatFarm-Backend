package com.goatfarm.model;

/** The authenticated principal extracted from the JWT. {@code farmId} is null for users who have not registered a farm yet. */
public record AuthUser(Long userId, Long farmId) {
}
