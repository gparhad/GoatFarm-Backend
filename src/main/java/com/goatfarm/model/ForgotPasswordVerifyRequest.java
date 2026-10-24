package com.goatfarm.model;

public record ForgotPasswordVerifyRequest(String email, String phone) {
}
