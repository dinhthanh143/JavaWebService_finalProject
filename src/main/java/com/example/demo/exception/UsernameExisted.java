package com.example.demo.exception;


public class UsernameExisted extends RuntimeException {

    public UsernameExisted() {
        super();
    }

    public UsernameExisted(String message) {
        super(message);
    }

    public UsernameExisted(String message, Throwable cause) {
        super(message, cause);
    }
}
