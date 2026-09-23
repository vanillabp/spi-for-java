package io.vanillabp.spi.process;

/**
 * What kind of BPMN element a {@link WorkflowElementHistory} entry stands for, so a viewer knows
 * how to draw it without reading the model first.
 * <p>
 * Most values name the element of the same name in BPMN. Three do not, and they are the ones to
 * read before switching over this enum: {@link #PROCESS} is the workflow itself rather than a step
 * in it, {@link #SEQUENCE_FLOW} is an arrow rather than a node, and {@link #MULTI_INSTANCE} is the
 * bracket a BPMS puts around a repeated activity, whose iterations arrive as entries of their own.
 * <p>
 * Which values ever turn up depends on the BPMS and on how much of a run it records, so handle an
 * unexpected one the way you handle {@link #UNKNOWN}. The list grows as BPMN and the engines do.
 */
public enum WorkflowElementType {
  /** The BPMS reported no type, or one this list has no value for. */
  UNKNOWN,
  /** The workflow itself: one entry covering the whole run rather than a step inside it. */
  PROCESS,
  /** An embedded subprocess, the box drawn around a part of the process. */
  SUB_PROCESS,
  /** A subprocess the engine starts on its own when the event it waits for happens. */
  EVENT_SUB_PROCESS,
  /** A subprocess whose steps run in an order the model does not prescribe. */
  AD_HOC_SUB_PROCESS,
  /** A start event, of the process or of a subprocess. */
  START_EVENT,
  /** An event the workflow waits at, for a message or a timer for example. */
  INTERMEDIATE_CATCH_EVENT,
  /** An event the workflow throws in passing, a signal or an escalation for example. */
  INTERMEDIATE_THROW_EVENT,
  /** An event attached to the border of an activity, which may interrupt it. */
  BOUNDARY_EVENT,
  /** An end event, of the process or of a subprocess. */
  END_EVENT,
  /**
   * A service task: the kind a {@link io.vanillabp.spi.service.WorkflowTask} method usually
   * serves.
   */
  SERVICE_TASK,
  /** A task which waits for a message to be correlated. */
  RECEIVE_TASK,
  /** A task which waits for a person. */
  USER_TASK,
  /** Work a person does outside any system, so the engine only records that it happened. */
  MANUAL_TASK,
  /** BPMN's undefined task, the one whose kind the model leaves open. */
  TASK,
  /** A gateway taking exactly one of its outgoing paths. */
  EXCLUSIVE_GATEWAY,
  /** A gateway taking every outgoing path whose condition holds. */
  INCLUSIVE_GATEWAY,
  /** A gateway taking all of its outgoing paths at once. */
  PARALLEL_GATEWAY,
  /** A gateway where the first of the following events decides the path. */
  EVENT_BASED_GATEWAY,
  /** An arrow between two elements. Only a BPMS which records the arrows themselves reports it. */
  SEQUENCE_FLOW,
  /**
   * The bracket a BPMS puts around a repeated activity. The iterations are entries of their own,
   * carrying the type of the repeated element.
   */
  MULTI_INSTANCE,
  /**
   * An activity running another process. Its entry carries the history context to dig into that
   * run, once the call has started one.
   */
  CALL_ACTIVITY,
  /** A task asking a decision model. */
  BUSINESS_RULE_TASK,
  /** A task the engine carries out itself, by running a script of the model. */
  SCRIPT_TASK,
  /** A task sending a message. */
  SEND_TASK,
  /** A subprocess whose work is compensated as a whole when it is canceled. */
  TRANSACTION
}
