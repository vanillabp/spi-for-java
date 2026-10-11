package io.vanillabp.spi.process;

import java.io.InputStream;
import java.util.List;

import io.vanillabp.spi.service.TaskId;

/**
 * <p>
 * Two promises of this interface are written down in the decision log, because several places
 * rely on them. A workflow is addressed by the id attribute of its aggregate, not by a technical
 * key (decision 2 in the repository's DECISIONS.md). And a broadcast signal reaches the workflow
 * module of THIS service and no other (decision 6 in the repository's DECISIONS.md).
 *
 * @param <A> The workflow-aggregate-class
 */
public interface ProcessService<A> {

  /**
   * Start a new workflow.
   *
   * @param workflowAggregate The workflow-aggregate
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   */
  A startWorkflow(
      A workflowAggregate);

  /**
   * Starts a new workflow by message start event.
   * <p>
   * The content of the message is <i>never</i> sent to the BPMS. The workflow
   * aggregate is the single source of truth. BPMN logic (expressions,
   * conditions) reads data from the workflow aggregate, not from message
   * payloads. Copy any data of the incoming message into the workflow
   * aggregate before you call this method.
   * <p>
   * The message has to fire a message start event of the process of THIS process service. Some
   * adapters know the model, for example on Camunda 7 and Camunda 8. There a message which does
   * not start that process is refused with an {@link IllegalArgumentException}, and nothing is
   * started. Version 1 started any process which knew the message.
   *
   * @param workflowAggregate The workflow-aggregate
   * @param messageName The message name
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   */
  default A startWorkflowByMessage(
      A workflowAggregate,
      String messageName) {
    throw new UnsupportedOperationException(
        "startWorkflowByMessage is implemented by a VanillaBP adapter");
  }

  /**
   * Tells the BPMS that the workflow-aggregate changed, so it sees the current
   * state before it evaluates anything next.
   * <p>
   * VanillaBP pushes the aggregate at the points where it talks to the BPMS anyway:
   * when a workflow starts, when a task is completed and when a message is
   * correlated. Between these points the BPMS still holds the state of the last
   * push. That matters when a conditional event waits for exactly this change, or
   * when a gateway is evaluated before your next task runs.
   * <p>
   * This method does not decide WHAT is pushed. That is the part of the aggregate
   * shared with the BPMS ({@link io.vanillabp.spi.service.SyncWithBPMS} /
   * {@link io.vanillabp.spi.service.NoSyncWithBPMS}). The method names no
   * variables, so the aggregate stays the single source of truth.
   * <p>
   * What a BPMS DOES with the new values is up to the BPMS. Camunda 7 evaluates the
   * conditions of waiting conditional events again. Other systems just keep the
   * values until something reads them.
   *
   * @param workflowAggregate The workflow-aggregate
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   */
  default A aggregateChanged(
      A workflowAggregate) {
    throw new UnsupportedOperationException(
        "aggregateChanged is implemented by a VanillaBP adapter");
  }

  /**
   * Tells the BPMS that the workflow-aggregate changed. The shared values go into
   * the scope the given task RUNS IN, not into the global scope of the workflow.
   * <p>
   * That scope is the one the task belongs to: the process, an embedded subprocess,
   * or one iteration of a multi-instance embedded subprocess. The rest of that scope
   * evaluates these values, and an event subprocess with a conditional start event
   * listens on them. The task's own context is NOT used, on purpose. Values written
   * there would serve a boundary event of that task and nothing else, and they are
   * gone when the task is gone.
   * <p>
   * Multi-instance work needs this. Every iteration has a scope of its own, and a
   * write for the whole workflow would let one iteration overwrite the values of
   * another.
   * <p>
   * <b>It does not write the global scope as well.</b> A value written in an inner
   * scope hides the global one there anyway. Writing both would change what the
   * OTHER iterations see, and multi-instance code must never do that by accident.
   * Keep the result in mind: the global values of the workflow stay as they were.
   * So a gateway AFTER the multi-instance sees the older state, unless you call
   * {@link #aggregateChanged(Object)} as well.
   *
   * @param workflowAggregate The workflow-aggregate
   * @param taskId The task-id reported previously
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  default A aggregateChanged(
      A workflowAggregate,
      String taskId) {
    throw new UnsupportedOperationException(
        "aggregateChanged is implemented by a VanillaBP adapter");
  }

  /**
   * Broadcasts a BPMN signal.
   * <p>
   * A signal is a broadcast by definition. Every element waiting for it reacts,
   * and processes with a signal start event are started. So it is NOT addressed
   * to one workflow. Unlike {@link #correlateMessage(Object, String)}, this method
   * takes no workflow aggregate, and there is no way to limit a signal to a single
   * workflow. A BPMS which can do that is the exception, not the rule.
   * <p>
   * <b>The broadcast is limited to the WORKFLOW MODULE of this service.</b> It
   * reaches every BPMS the module is deployed to. That keeps a broadcast complete
   * while workflows move from one BPMS to another. It also reaches every process of
   * the module which waits for that signal. If an application wants a signal in
   * several modules, it sends it through the {@code ProcessService} of each of them.
   * VanillaBP does not decide that for you, because which modules are meant is a
   * business question.
   * <p>
   * The name-clash-avoidance mode of the module decides whether the signal really
   * stays inside that module. A tenant or a prefixed signal name keeps it there. The mode
   * {@code none} limits nothing, so the signal reaches every subscription of that
   * BPMS with the same name, whichever module it belongs to. That is the price of
   * this mode, not a property of signals. It is also the reason why VanillaBP asks
   * for a decision at startup if a module runs without a limit.
   * <p>
   * Pass the signal name as it is written in the model. VanillaBP adds whatever
   * name scoping the workflow module uses. It talks to the BPMS with the tenant and
   * the client configured for the adapter the module belongs to.
   * <p>
   * No payload travels with the signal. Like a message, a signal carries its name
   * and nothing else, because the workflow aggregate is the single source of truth.
   * <p>
   * <b>A signal is not buffered.</b> It reaches whoever waits for it at that very
   * moment. A workflow which reaches its catch event a moment later gets nothing.
   * If a delivery has to wait for its recipient, correlate a message to that
   * workflow instead.
   *
   * @param signalName The BPMN signal name
   */
  default void sendSignal(
      String signalName) {
    throw new UnsupportedOperationException(
        "sendSignal is implemented by a VanillaBP adapter");
  }

  /**
   * Correlates a message to the workflow of the workflow-aggregate, or to one of its
   * sub-workflows (call activities).
   * <p>
   * The content of the message is <i>never</i> sent to the BPMS. The workflow
   * aggregate is the single source of truth. BPMN logic (expressions,
   * conditions) reads data from the workflow aggregate, not from message
   * payloads. Copy any data of the incoming message into the workflow
   * aggregate before you call this method.
   *
   * @param workflowAggregate The workflow-aggregate
   * @param messageName  The message name to be correlated
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   */
  A correlateMessage(
      A workflowAggregate,
      String messageName);

  /**
   * Correlates a message to the workflow of the workflow-aggregate, or to one of its
   * sub-workflows (call activities).
   * <p>
   * The content of the message is <i>never</i> sent to the BPMS. The workflow
   * aggregate is the single source of truth. BPMN logic (expressions,
   * conditions) reads data from the workflow aggregate, not from message
   * payloads. Copy any data of the incoming message into the workflow
   * aggregate before you call this method.
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param messageName   The message name to be correlated
   * @param correlationId The correlation-id
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   */
  A correlateMessage(
      A workflowAggregate,
      String messageName,
      String correlationId);

  /**
   * Completes a user task.
   *
   * @param workflowAggregate The workflow-aggregate
   * @param taskId       The task-id reported previously
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A completeUserTask(
      A workflowAggregate,
      String taskId);

  /**
   * Ends a user task by sending a BPMN error.
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param taskId        The task-id reported previously
   * @param bpmnErrorCode The error code. An error boundary event in the BPMN model
   *                      can catch it
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A cancelUserTask(
      A workflowAggregate,
      String taskId,
      String bpmnErrorCode);

  /**
   * Completes an asynchronous task.
   *
   * @param workflowAggregate The workflow-aggregate
   * @param taskId            The task-id reported previously
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A completeTask(
      A workflowAggregate,
      String taskId);

  /**
   * Ends an asynchronous task by sending a BPMN error.
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param taskId        The task-id reported previously
   * @param bpmnErrorCode The error code. An error boundary event in the BPMN model
   *                      can catch it
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A cancelTask(
      A workflowAggregate,
      String taskId,
      String bpmnErrorCode);

  /**
   * The ID of the <a href="https://github.com/vanillabp/adapter-platform-integration/wiki/Workflow-modules" target="_blank">workflow-module</a>
   * this process service belongs to.
   *
   * @return The workflow-module ID
   */
  String getWorkflowModuleId();

  /**
   * Returns all process definitions of the workflow of the given aggregate.
   *
   * @param workflowAggregate The workflow-aggregate for which to get the process definitions
   * @param historyContext Null for the primary process of the workflow. For a secondary
   *                       process of the workflow (a call activity), a value from
   *                       {@link WorkflowElementHistory#secondaryWorkflowHistoryContext()}
   * @return The process definitions
   * @throws WorkflowNotFoundException If no workflow belongs to the workflow-aggregate
   */
  default List<ProcessDefinition> getProcessDefinitions(
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException {
    throw new UnsupportedOperationException(
        "getProcessDefinitions is implemented by a VanillaBP adapter");
  }

  /**
   * Returns the BPMN XML of a process definition.
   *
   * @param processDefinitionId The process definition id
   * @return The BPMN XML as an input stream
   * @throws ProcessDefinitionNotFoundException If the process definition is not found
   */
  default InputStream getBpmnXml(
      String processDefinitionId) throws ProcessDefinitionNotFoundException {
    throw new UnsupportedOperationException(
        "getBpmnXml is implemented by a VanillaBP adapter");
  }

  /**
   * Returns the history of the workflow which belongs to the given aggregate.
   *
   * @param workflowAggregate The workflow-aggregate for which to get the history
   * @param historyContext Null for the primary process of the workflow. For a secondary
   *                       process of the workflow (a call activity), a value from
   *                       {@link WorkflowElementHistory#secondaryWorkflowHistoryContext()}
   * @return The workflow history
   * @throws WorkflowNotFoundException If no workflow belongs to the workflow-aggregate
   */
  default WorkflowHistory getWorkflowHistory(
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException {
    throw new UnsupportedOperationException(
        "getWorkflowHistory is implemented by a VanillaBP adapter");
  }

}
