package com.goatfarm.exceptionHandler;

public class InvalidCredentialException extends RuntimeException{

    public InvalidCredentialException(){
        super("Invalid credentials");
    }

    public InvalidCredentialException(String message)
    {
        super(message);
    }
}
