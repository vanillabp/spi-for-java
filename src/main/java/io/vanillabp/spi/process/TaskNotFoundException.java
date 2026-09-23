package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#completeTask(Object, String)} and
 * {@link ProcessService#cancelTask(Object, String, String)} if no configured BPMS
 * knows the given task: it never existed, its ID is wrong, or it was removed
 * without completion (e.g. the workflow was canceled).
 */
public class TaskNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Reports a task which no configured BPMS knows. A task which is merely completed already is
   * not reported this way: answering it twice is a logged no-op.
   *
   * @param message What the application is told. Name the task id, the adapters which were asked
   *                and in which order, because that is all the caller gets to see
   */
  public TaskNotFoundException(
      final String message) {

    super(message);

  }

}
