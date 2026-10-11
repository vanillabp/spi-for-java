package io.vanillabp.spi.service;

/**
 * Reports a business error which the BPMN model handles, for example with an error boundary event.
 * <p>
 * See decision 4 in the repository's DECISIONS.md. It says why this is a business outcome and
 * not a failure, and why the aggregate is committed. The same decision is the reason why a
 * workflow service must not have a transaction of its own.
 */
public class TaskException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** What the BPMS shows as the message of the error. */
  private final String errorName;

  /** What an error boundary event is matched by. */
  private final String errorCode;

  /* an API exception needs no stack trace, and building one is expensive */
  @Override
  public synchronized Throwable fillInStackTrace() {

    return this;

  }

  /**
   * Reports the outcome under one name. That name is both the code which routes the workflow and
   * the message the BPMS shows.
   *
   * @param errorCode The code of the BPMN error the model is to catch
   */
  public TaskException(
      final String errorCode) {

    super(errorCode);
    this.errorName = errorCode;
    this.errorCode = errorCode;

  }

  /**
   * Reports the outcome with a readable name and a separate code. Only the code routes the
   * workflow. The name reaches the BPMS as the message of the error, which is what somebody who
   * looks into the engine gets to read.
   *
   * @param errorName What the BPMS shows as the message of the error
   * @param errorCode The code of the BPMN error the model is to catch
   */
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

  /**
   * The readable part of the outcome.
   *
   * @return The name given to the constructor, or the code where only one value was given
   */
  public String getErrorName() {

    return errorName;

  }

  /**
   * The part which routes the workflow. The BPMS compares it with the error codes of the boundary
   * events around the task.
   *
   * @return The code of the BPMN error the model is to catch
   */
  public String getErrorCode() {

    return errorCode;

  }

}
