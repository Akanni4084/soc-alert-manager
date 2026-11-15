package com.abdulmanan.exception;

public class InvalidAlertOperationException extends RuntimeException {

    public InvalidAlertOperationException(String message) {
        super(message);
    }
}