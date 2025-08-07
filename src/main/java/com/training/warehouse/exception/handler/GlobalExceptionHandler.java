package com.training.warehouse.exception.handler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.ConflicException;
import com.training.warehouse.exception.ForbiddenException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.exception.UnauthorizedException;

@ControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException exception) {
                List<String> messages = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .toList();
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ExceptionResponse.builder()
                                                .messages(messages)
                                                .build());
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException exception) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ExceptionResponse.builder()
                                                .messages(List.of(exception.getMessage()))
                                                .build());
        }

        @ExceptionHandler(ConflicException.class)
        public ResponseEntity<ExceptionResponse> handleConflicException(ConflicException exception) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ExceptionResponse.builder()
                                                .messages(List.of(exception.getMessage()))
                                                .build());
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ExceptionResponse> handleForbiddenException(ForbiddenException exception) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ExceptionResponse.builder()
                                                .messages(List.of(exception.getMessage()))
                                                .build());
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException exception) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ExceptionResponse.builder()
                                                .messages(List.of(exception.getMessage()))
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

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ExceptionResponse.builder()
                                                .messages(List.of(exception.getMessage()))
                                                .build());
        }
}
