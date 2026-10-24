package com.goatfarm.model;

public record ForgotPasswordVerifyResponse(boolean success, String message, String resetToken) {

    @Override
    public String toString() {
        return "ForgotPasswordVerifyResponse[success=" + success + ", message=" + message + ", resetToken=***]";
    }
}
