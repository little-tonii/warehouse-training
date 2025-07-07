package com.training.warehouse.exception.handler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.training.warehouse.exception.UnauthorizedException;
import com.training.warehouse.exception.UserAlreadyExistException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ExceptionResponse.builder()
                .messages(List.of(exception.getMessage()))
                .build());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ExceptionResponse.builder()
                .messages(List.of(exception.getMessage()))
                .build());
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ExceptionResponse> handleUserAlreadyExistException(UserAlreadyExistException exception) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ExceptionResponse.builder()
                .messages(List.of(exception.getMessage()))
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getDefaultMessage())
            .toList();
        return ResponseEntity
            .badRequest()
            .body(ExceptionResponse.builder()
                .messages(messages)
                .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ExceptionResponse.builder()
                .messages(List.of(exception.getMessage()))
                .build());
    }
}
