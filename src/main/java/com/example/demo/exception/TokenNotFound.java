package com.example.demo.exception;

public class TokenNotFound extends RuntimeException{
    public TokenNotFound() {
        super();
    }

    public TokenNotFound(String message) {
        super(message);
    }

    public TokenNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
