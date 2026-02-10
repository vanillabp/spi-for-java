package io.vanillabp.spi.process;

public class ProcessDefinitionNotFoundException extends RuntimeException {

  public ProcessDefinitionNotFoundException(
      final String message) {
    super(message);
  }

  public ProcessDefinitionNotFoundException(
      final String message,
      final Throwable cause) {
    super(message, cause);
  }

}
