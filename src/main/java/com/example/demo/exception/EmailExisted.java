package com.example.demo.exception;

public class EmailExisted extends RuntimeException{
    public EmailExisted(String message) {
        super(message);
    }

    public EmailExisted() {
        super();
    }

    public EmailExisted(String message, Throwable cause) {
        super(message, cause);
    }
}
