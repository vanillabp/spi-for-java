package io.vanillabp.spi.process;

/**
 * What kind of BPMN element a {@link WorkflowElementHistory} entry stands for. A viewer knows how
 * to draw it without reading the model first.
 * <p>
 * Most values name the element of the same name in BPMN. Three do not. Read about them before you
 * switch over this enum. {@link #PROCESS} is the workflow itself, not a step in it.
 * {@link #SEQUENCE_FLOW} is an arrow, not a node. {@link #MULTI_INSTANCE} is the bracket a BPMS
 * puts around a repeated activity, and the iterations arrive as entries of their own.
 * <p>
 * Which values turn up depends on the BPMS and on how much of a run it records. So handle an
 * unexpected one the way you handle {@link #UNKNOWN}. The list grows as BPMN and the engines
 * grow.
 */
public enum WorkflowElementType {
  /** The BPMS reported no type, or one this list has no value for. */
  UNKNOWN,
  /** The workflow itself: one entry covering the whole run rather than a step inside it. */
  PROCESS,
  /** An embedded subprocess, the box drawn around a part of the process. */
  SUB_PROCESS,
  /** A subprocess which the engine starts by itself when the event it waits for happens. */
  EVENT_SUB_PROCESS,
  /** A subprocess whose steps run in an order the model does not prescribe. */
  AD_HOC_SUB_PROCESS,
  /** A start event, of the process or of a subprocess. */
  START_EVENT,
  /** An event the workflow waits at, for a message or a timer for example. */
  INTERMEDIATE_CATCH_EVENT,
  /** An event the workflow throws on its way, a signal or an escalation for example. */
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
  /** Work a person does outside any system. The engine only records that it happened. */
  MANUAL_TASK,
  /** The undefined task of BPMN. The model does not say what kind of task it is. */
  TASK,
  /** A gateway taking exactly one of its outgoing paths. */
  EXCLUSIVE_GATEWAY,
  /** A gateway taking every outgoing path whose condition holds. */
  INCLUSIVE_GATEWAY,
  /** A gateway taking all of its outgoing paths at once. */
  PARALLEL_GATEWAY,
  /** A gateway where the first event that happens after it decides the path. */
  EVENT_BASED_GATEWAY,
  /** An arrow between two elements. Only a BPMS which records the arrows themselves reports it. */
  SEQUENCE_FLOW,
  /**
   * The bracket a BPMS puts around a repeated activity. The iterations are entries of their own,
   * carrying the type of the repeated element.
   */
  MULTI_INSTANCE,
  /**
   * An activity which runs another process. Once the call has started that process, the entry
   * carries the history context to look into its run.
   */
  CALL_ACTIVITY,
  /** A task which asks a decision model. */
  BUSINESS_RULE_TASK,
  /** A task the engine does itself, by running a script of the model. */
  SCRIPT_TASK,
  /** A task which sends a message. */
  SEND_TASK,
  /** A subprocess whose work is undone as a whole (compensated) when it is canceled. */
  TRANSACTION
}
