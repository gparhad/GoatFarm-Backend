package com.goatfarm.exception;

/** A JWT is expired, malformed or has a bad signature. Raised by JwtUtil, handled inside the security filter. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
