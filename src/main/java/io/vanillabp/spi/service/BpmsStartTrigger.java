package io.vanillabp.spi.service;

/**
 * Which start event started a workflow that nobody started through VanillaBP. A
 * {@link WorkflowStartedByBpms} method gets it if it declares a parameter of this type.
 * <p>
 * There is no time in here. The moment a start event fired is up to the application. Model
 * it as a process variable, fill it by an expression in the BPMN model and read it as a
 * <code>&#64;TaskParam</code>. An adapter would have to make up that time, because no BPMS
 * tells a start listener the time it planned the start for. Decision 9 in the repository's
 * DECISIONS.md says why the time was removed and why every kind of start event is in here.
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
   * {@link WorkflowStartedByBpms} method. What decides is the state of the workflow, not
   * the kind of its start event. A workflow which VanillaBP started already has its id and
   * its workflow aggregate. A workflow without them was started past VanillaBP, whichever
   * event began it.
   */
  public enum Kind {

    /** A plain start event, without any event definition. */
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
