package com.training.warehouse.exception;

public class InvalidInboundStatusException extends RuntimeException {
  public InvalidInboundStatusException(String message) {
    super(message);
  }
}
