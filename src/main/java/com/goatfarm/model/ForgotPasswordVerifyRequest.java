package com.goatfarm.model;

import lombok.Data;

@Data
public class ForgotPasswordVerifyRequest {
    private String username;
    private String phone;
}
