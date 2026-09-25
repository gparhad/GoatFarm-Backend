package com.goatfarm.model;

import lombok.Data;

@Data
public class ForgotPasswordVerifyRequest {
    private String email;
    private String phone;
}
