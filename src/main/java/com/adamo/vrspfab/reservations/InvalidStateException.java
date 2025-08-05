package com.adamo.vrspfab.reservations;

public class InvalidStateException extends RuntimeException {
  public InvalidStateException(String message) {
    super(message);
  }
}