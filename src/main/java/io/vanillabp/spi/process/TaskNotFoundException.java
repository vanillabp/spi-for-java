package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#completeTask(Object, String)} and
 * {@link ProcessService#cancelTask(Object, String, String)} if no configured BPMS
 * knows the given task. Maybe it never existed, or its ID is wrong, or it was
 * removed without being completed (e.g. the workflow was canceled).
 * <p>
 * A BPMS adapter throws the same type in the first phase of such a call, if its own check finds
 * that the task is gone. So the application gets one type, whichever of the two found out, and
 * catches just that. Often only the adapter can find out at all. VanillaBP writes the mark that
 * a task is closed itself, so a task which somebody completed past VanillaBP still has a record
 * which says it is open.
 */
public class TaskNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Reports a task which no configured BPMS knows. A task which is just completed already is not
   * reported this way. Answering it a second time does nothing except write a log line.
   *
   * @param message What the application is told. Name the task id and the adapters which were
   *                asked, in their order, because the caller sees nothing else
   */
  public TaskNotFoundException(
      final String message) {

    super(message);

  }

}
