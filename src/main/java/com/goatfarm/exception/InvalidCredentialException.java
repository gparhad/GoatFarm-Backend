package com.goatfarm.exception;

/** Login failed. Mapped to HTTP 401. Deliberately gives no hint whether the user exists. */
public class InvalidCredentialException extends RuntimeException {

    public InvalidCredentialException() {
        super("Invalid credentials");
    }

    public InvalidCredentialException(String message) {
        super(message);
    }
}
