package com.goatfarm.model;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String resetToken;
    private String newPassword;
}
