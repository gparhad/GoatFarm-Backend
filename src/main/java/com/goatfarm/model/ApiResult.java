package com.goatfarm.model;

/** Envelope {@code {success, message, data}} used by the pregnancy endpoints. */
public record ApiResult<T>(boolean success, String message, T data) {

    public static <T> ApiResult<T> ok(String message, T data) {
        return new ApiResult<>(true, message, data);
    }
}
