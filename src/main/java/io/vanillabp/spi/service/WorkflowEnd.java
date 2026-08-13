package io.vanillabp.spi.service;

import java.time.Instant;

/**
 * How a workflow ended - handed to a {@link WorkflowEnded} method which declares a
 * parameter of this type.
 *
 * @param kind How it ended, as far as the BPMS reports it
 * @param time When it ended, as the BPMS reports it or - where it does not - the
 *          moment VanillaBP was notified
 * @param endEventId The BPMN id of the end event reached, or <code>null</code>
 *          where the BPMS does not report which one it was
 */
public record WorkflowEnd(
                          Kind kind,
                          Instant time,
                          String endEventId) {

  /**
   * How a workflow ended. Which of these a BPMS can tell apart differs; an adapter
   * whose BPMS cannot distinguish them reports {@link #COMPLETED} and its
   * documentation says so - a faked distinction would be worse than none.
   */
  public enum Kind {

    /** The workflow reached an end event. */
    COMPLETED,

    /**
     * The workflow was ended without reaching an end event: cancelled or deleted
     * by an operator, terminated by a terminate end event, or interrupted by an
     * event of an enclosing scope.
     */
    TERMINATED

  }

}
