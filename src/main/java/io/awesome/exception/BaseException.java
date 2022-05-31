package io.awesome.exception;

public class BaseException extends RuntimeException {
  public BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
