package io.vanillabp.spi.process;

/**
 * Thrown if no configured BPMS knows a workflow for the given aggregate. It comes from the
 * {@link ProcessService} methods which address a workflow that already exists:
 * {@code correlateMessage}, {@code aggregateChanged}, {@code getProcessDefinitions} and
 * {@code getWorkflowHistory}. Maybe the workflow never started. Maybe it ended so long ago that
 * the BPMS has cleaned it up. Or the aggregate carries another id than the one it was started
 * with.
 * <p>
 * For the two reading methods, an ended workflow is not an error. A BPMS still answers with the
 * definitions and the history of a workflow it has completed.
 * <p>
 * {@code correlateMessage} and {@code aggregateChanged} save the aggregate before they ask a BPMS.
 * So when this exception arrives, what your code wrote is already saved. Only the step in the
 * workflow did not happen.
 */
public class WorkflowNotFoundException extends RuntimeException {

  /**
   * Reports a workflow which no configured BPMS knows.
   *
   * @param message What the application is told. Name the aggregate and the adapters which were
   *                asked, in their order, because the caller sees nothing else
   */
  public WorkflowNotFoundException(
      final String message) {
    super(message);
  }

}
