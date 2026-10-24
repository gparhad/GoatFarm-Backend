package com.goatfarm.model;

public record ResetPasswordRequest(String resetToken, String newPassword) {

    @Override
    public String toString() {
        return "ResetPasswordRequest[resetToken=***, newPassword=***]";
    }
}
