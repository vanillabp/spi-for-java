package io.vanillabp.spi.service;

import java.time.Instant;

/**
 * How a workflow ended. A {@link WorkflowEnded} method gets it if it declares a
 * parameter of this type.
 *
 * @param kind How it ended, as far as the BPMS reports it
 * @param time When it ended, as the BPMS reports it. If the BPMS does not report
 *          it, the moment VanillaBP was notified
 * @param endEventId The BPMN id of the end event reached, or <code>null</code>
 *          if the BPMS does not report which one it was
 */
public record WorkflowEnd(
                          Kind kind,
                          Instant time,
                          String endEventId) {

  /**
   * How a workflow ended. Not every BPMS can tell these apart. An adapter whose
   * BPMS cannot tell them apart reports {@link #COMPLETED}, and its documentation
   * says so. A made-up difference would be worse than none.
   */
  public enum Kind {

    /** The workflow reached an end event. */
    COMPLETED,

    /**
     * The workflow ended without reaching an end event. The BPMS decides which
     * of the modelled paths lead here. What one engine reports as canceled,
     * another may report as completed. The documentation of each adapter says
     * what its BPMS reports.
     */
    CANCELED

  }

}
