package com.goatfarm.model;

public record LoginRequest(String email, String password) {

    /** Keeps the password out of logs (the logging aspect prints argument toString()). */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
