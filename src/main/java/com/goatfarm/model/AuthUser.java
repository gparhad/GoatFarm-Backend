package com.goatfarm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthUser {
    private Long userId;
    private Long farmId;
}
