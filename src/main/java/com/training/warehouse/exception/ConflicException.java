package com.training.warehouse.exception;

public class ConflicException extends RuntimeException {
    public ConflicException(String message) {
        super(message);
    }
}
