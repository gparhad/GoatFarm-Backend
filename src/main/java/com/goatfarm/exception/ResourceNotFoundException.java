package com.goatfarm.exception;

/** A requested aggregate (goat, farm, record, user ...) does not exist in the caller's scope. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
