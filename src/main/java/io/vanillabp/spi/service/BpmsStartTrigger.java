package io.vanillabp.spi.service;

import java.time.Instant;

/**
 * What made the BPMS start a workflow on its own - handed to a
 * {@link WorkflowStartedByBpms} method which declares a parameter of this type.
 *
 * @param kind Which kind of start event fired
 * @param time When it fired: a timer's scheduled time as the engine reports it,
 *          otherwise the moment VanillaBP was notified
 * @param signalName The name of the signal for {@link Kind#SIGNAL},
 *          <code>null</code> otherwise
 * @param startEventId The BPMN id of the start event which fired
 */
public record BpmsStartTrigger(
                               Kind kind,
                               Instant time,
                               String signalName,
                               String startEventId) {

  /**
   * The kinds of start event a BPMS fires without the application asking for it.
   * Message start events are NOT among them: those are triggered by the
   * application through
   * {@link io.vanillabp.spi.process.ProcessService#startWorkflowByMessage(Object, String)},
   * which carries the aggregate.
   */
  public enum Kind {

    /** A timer start event, including cyclic ones. */
    TIMER,

    /** A signal start event, fired by a broadcast signal. */
    SIGNAL,

    /** A conditional start event whose condition became true. */
    CONDITIONAL

  }

}
