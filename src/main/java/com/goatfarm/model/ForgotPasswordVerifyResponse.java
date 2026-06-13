package com.goatfarm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordVerifyResponse {
    private boolean success;
    private String message;
    private String resetToken;
}
