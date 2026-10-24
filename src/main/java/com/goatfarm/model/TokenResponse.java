package com.goatfarm.model;

public record TokenResponse(String token) {

    @Override
    public String toString() {
        return "TokenResponse[token=***]";
    }
}
