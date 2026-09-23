package io.vanillabp.spi.process;

/**
 * Thrown by the {@link ProcessService} methods which address a workflow that already exists -
 * {@code correlateMessage}, {@code aggregateChanged}, {@code getProcessDefinitions} and
 * {@code getWorkflowHistory} - if no configured BPMS knows a workflow for the aggregate handed
 * over. It never started, it ended long enough ago for the BPMS to have cleaned it up, or the
 * aggregate carries a different id than the one it was started with.
 * <p>
 * An ended workflow is not an error for the two reading methods: a BPMS still answers with the
 * definitions and the history of a workflow it has completed.
 * <p>
 * {@code correlateMessage} and {@code aggregateChanged} save the aggregate before they ask a BPMS,
 * so what your code wrote is written when this arrives. What did not happen is the step in the
 * workflow.
 */
public class WorkflowNotFoundException extends RuntimeException {

  /**
   * Reports a workflow which no configured BPMS knows.
   *
   * @param message What the application is told. Name the aggregate, the adapters which were
   *                asked and in which order, because that is all the caller gets to see
   */
  public WorkflowNotFoundException(
      final String message) {
    super(message);
  }

}
