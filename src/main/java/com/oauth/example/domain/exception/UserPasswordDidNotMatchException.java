package com.oauth.example.domain.exception;

public class UserPasswordDidNotMatchException extends RuntimeException {
    public UserPasswordDidNotMatchException(String message) {
        super(message);
    }
}
