package io.vanillabp.spi.service;

/**
 * Used to indicate errors which have to be processed by the BPMN.
 * <p>
 * Why this is a business outcome with the aggregate committed, and not a failure, is decision 4 in
 * the repository's DECISIONS.md. That decision is also why a workflow service must not carry a
 * transaction of its own.
 */
public class TaskException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String errorName;

  private final String errorCode;

  /* avoid the expensive and useless stack trace for api exceptions */
  @Override
  public synchronized Throwable fillInStackTrace() {

    return this;

  }

  public TaskException(
      final String errorCode) {

    super(errorCode);
    this.errorName = errorCode;
    this.errorCode = errorCode;

  }

  public TaskException(
      final String errorName,
      final String errorCode) {

    super(errorName
        + " ("
        + errorCode
        + ")");
    this.errorName = errorName;
    this.errorCode = errorCode;

  }

  public String getErrorName() {

    return errorName;

  }

  public String getErrorCode() {

    return errorCode;

  }

}
