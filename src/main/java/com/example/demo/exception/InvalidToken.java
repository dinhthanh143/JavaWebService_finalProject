package com.example.demo.exception;

public class InvalidToken extends RuntimeException {
    public InvalidToken(String message) {
        super(message);
    }

    public InvalidToken(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidToken() {
        super();
    }
}
