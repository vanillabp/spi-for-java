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

  /** What the BPMS shows as the message of the error. */
  private final String errorName;

  /** What an error boundary event is matched by. */
  private final String errorCode;

  /* avoid the expensive and useless stack trace for api exceptions */
  @Override
  public synchronized Throwable fillInStackTrace() {

    return this;

  }

  /**
   * Reports the outcome under one name, which is then both the code routing the workflow and the
   * message the BPMS shows.
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
   * Reports the outcome where the model tells a readable name and a code apart. Only the code
   * routes the workflow; the name reaches the BPMS as the message of the error, which is what
   * somebody looking into the engine gets to read.
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
   * The readable half of the outcome.
   *
   * @return The name given to the constructor, or the code where only one value was given
   */
  public String getErrorName() {

    return errorName;

  }

  /**
   * The half which routes the workflow: the BPMS matches it against the error code of the
   * boundary events around the task.
   *
   * @return The code of the BPMN error the model is to catch
   */
  public String getErrorCode() {

    return errorCode;

  }

}
