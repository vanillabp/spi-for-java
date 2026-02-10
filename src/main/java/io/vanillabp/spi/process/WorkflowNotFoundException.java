package io.vanillabp.spi.process;

public class WorkflowNotFoundException extends RuntimeException {

  public WorkflowNotFoundException(
      final String message) {
    super(message);
  }

}
