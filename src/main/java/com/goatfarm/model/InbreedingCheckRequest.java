package com.goatfarm.model;

import jakarta.validation.constraints.NotBlank;

public record InbreedingCheckRequest(
        @NotBlank(message = "goatTagNumber is required") String goatTagNumber,
        @NotBlank(message = "breederTagNumber is required") String breederTagNumber) {
}
