package com.training.warehouse.exception;

public class FileNameIsNotValidException extends RuntimeException {
    public FileNameIsNotValidException(String message) {
        super(message);
    }
}