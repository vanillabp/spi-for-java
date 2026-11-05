package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#completeUserTask(Object, String)},
 * {@link ProcessService#cancelUserTask(Object, String, String)},
 * {@link ProcessService#completeTask(Object, String)} and
 * {@link ProcessService#cancelTask(Object, String, String)} if the workflow of the task is
 * suspended. Somebody stopped it in the BPMS, for example an operator in the tool of the BPMS.
 * While it is suspended, the BPMS refuses every answer to one of its tasks.
 * <p>
 * The exception comes before your transaction commits. So the transaction rolls back, nothing of
 * it is saved, and the task stays open. Make the same call again once the workflow is resumed.
 * <p>
 * Your transaction may also commit just before the workflow is suspended. Then you do not get this
 * exception. VanillaBP keeps the answer and gives it to the BPMS after the workflow is resumed. The
 * answer waits as long as the workflow is suspended, and the wait does not count as a failed
 * attempt. The same happens if the BPMS reports the suspension a moment late, which a remote BPMS
 * may do.
 * <p>
 * Not every BPMS can suspend a workflow, so not every adapter throws this. And a suspension is a
 * matter of one workflow: a workflow started by a call activity is not suspended together with the
 * workflow which called it, so its tasks can still be answered. The wiki of each adapter says what
 * its BPMS does. Why this is a type of its own is written down in
 * decision 10 in the repository's DECISIONS.md.
 */
public class WorkflowSuspendedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Reports a task whose workflow is suspended.
   *
   * @param message What the application is told. Name the task id and the adapter which holds it,
   *                and say that the same call can be made again after the workflow is resumed
   */
  public WorkflowSuspendedException(
      final String message) {

    super(message);

  }

  /**
   * Reports a task whose workflow is suspended, together with what the BPMS answered.
   *
   * @param message What the application is told. Name the task id and the adapter which holds it,
   *                and say that the same call can be made again after the workflow is resumed
   * @param cause   What the BPMS threw when it refused the call
   */
  public WorkflowSuspendedException(
      final String message,
      final Throwable cause) {

    super(message, cause);

  }

}
