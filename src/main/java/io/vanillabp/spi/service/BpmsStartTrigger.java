package io.vanillabp.spi.service;

/**
 * Which start event started a workflow nobody started through VanillaBP - handed to a
 * {@link WorkflowStartedByBpms} method which declares a parameter of this type.
 * <p>
 * There is no time in here. The moment a start event fired is the application's own
 * business: model it as a process variable, fill it by an expression in the BPMN model and
 * read it as a <code>&#64;TaskParam</code>. An adapter would have to invent that time,
 * because no BPMS hands a start listener the time it scheduled the start for.
 *
 * @param kind Which kind of start event fired
 * @param signalName The name of the signal for {@link Kind#SIGNAL},
 *          <code>null</code> otherwise
 * @param startEventId The BPMN id of the start event which fired
 */
public record BpmsStartTrigger(
                               Kind kind,
                               String signalName,
                               String startEventId) {

  /**
   * The kinds of start event a workflow can begin with. All of them reach a
   * {@link WorkflowStartedByBpms} method, because what makes a start reach it is the
   * state of the workflow and not the kind of its start event: a workflow VanillaBP
   * started already carries its id and its workflow aggregate, and one which does not
   * was started past VanillaBP, whichever event began it.
   */
  public enum Kind {

    /** A plain start event, carrying no event definition at all. */
    NONE,

    /** A message start event. */
    MESSAGE,

    /** A timer start event, including cyclic ones. */
    TIMER,

    /** A signal start event, fired by a broadcast signal. */
    SIGNAL,

    /** A conditional start event whose condition became true. */
    CONDITIONAL

  }

}
