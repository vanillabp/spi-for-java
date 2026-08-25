package io.vanillabp.spi.process;

import java.io.InputStream;
import java.util.List;

import io.vanillabp.spi.service.TaskId;

/**
 * <p>
 * Two promises of this interface are written down where several places rely on them: a workflow is
 * addressed by the id attribute of its aggregate rather than by a technical key (decision 2 in the
 * repository's DECISIONS.md), and a broadcast signal reaches the workflow module of THIS service
 * and no other (decision 6 in the repository's DECISIONS.md).
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
   * The message's content is <i>never</i> transmitted to the BPMS: the
   * workflow aggregate is the single source of truth. BPMN logic
   * (expressions, conditions) reads data from the workflow aggregate, not
   * from message payloads. Incorporate any data of the incoming message into
   * the workflow aggregate before calling this method.
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
   * state before the next thing it evaluates.
   * <p>
   * VanillaBP pushes the aggregate at the sync points it knows anyway (starting a
   * workflow, completing a task, correlating a message). Between them the BPMS
   * still holds the state of the last push - which matters when a conditional
   * event waits for exactly this change, or when a gateway is evaluated before the
   * next task of yours runs.
   * <p>
   * WHAT is pushed is not decided here: it is the part of the aggregate shared with
   * the BPMS ({@link io.vanillabp.spi.service.SyncWithBPMS} /
   * {@link io.vanillabp.spi.service.NoSyncWithBPMS}). This method names no
   * variables - the aggregate stays the single source of truth.
   * <p>
   * What a BPMS DOES with the new values is its own business: Camunda 7 re-evaluates
   * the conditions of waiting conditional events, other systems simply hold the
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
   * Tells the BPMS that the workflow-aggregate changed, pushing the shared values
   * into the scope the given task RUNS IN instead of the workflow's global scope.
   * <p>
   * That scope is the process, an embedded subprocess, or the one iteration of a
   * multi-instance embedded subprocess the task belongs to - what the rest of that
   * scope evaluates, and what an event subprocess with a conditional start event
   * listens on. Deliberately NOT the task's own context: values written there would
   * serve a boundary event of that task and nothing else, and they disappear with the
   * task.
   * <p>
   * This is what multi-instance work needs: every iteration has a scope of its own,
   * and a workflow-wide write would be a lost update between the iterations.
   * <p>
   * <b>It does not additionally write the global scope.</b> A value written in an
   * inner scope shadows the global one there anyway, and writing both would change
   * what the OTHER iterations see - which is the one thing multi-instance code must
   * not do by accident. The consequence is worth knowing: the workflow-global values
   * stay as they were, so a gateway AFTER the multi-instance evaluates the older
   * state unless you call {@link #aggregateChanged(Object)} as well.
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
   * A signal is a broadcast by definition: every element waiting for it reacts,
   * and processes having a signal start event are started. It is therefore NOT
   * addressed to one workflow - unlike
   * {@link #correlateMessage(Object, String)}, this method takes no workflow
   * aggregate, and there is no way to limit a signal to a single workflow (the
   * BPMS which can do that is the exception, not the rule).
   * <p>
   * <b>The broadcast is scoped to the WORKFLOW MODULE of this service.</b> It
   * reaches every BPMS the module is deployed to - which is what keeps a broadcast
   * complete while workflows are being migrated from one BPMS to another - and
   * every process of the module waiting for that signal. An application wanting a
   * signal in several modules sends it through the {@code ProcessService} of each of
   * them; VanillaBP does not decide that for you, because which modules are meant is
   * a business question.
   * <p>
   * How far the signal stays inside that module is what the module's
   * name-clash-avoidance mode decides. A tenant or a prefixed signal name keeps it
   * there; the mode {@code none} scopes nothing, so the signal reaches every
   * subscription of that BPMS carrying the same name, whichever module it belongs to.
   * That is the price of the mode rather than a property of signals, and the reason
   * VanillaBP asks for a decision at startup where a module runs unscoped.
   * <p>
   * Pass the signal name as it is modelled; VanillaBP applies whatever name
   * scoping the workflow module uses, and the BPMS is addressed with the tenant
   * and the client configured for the adapter it belongs to.
   * <p>
   * No payload travels with the signal: like a message, a signal transports its
   * name and nothing else - the workflow aggregate is the single source of truth.
   * <p>
   * <b>A signal is not buffered.</b> It reaches whoever waits for it at that very
   * moment; a workflow arriving at its catch event a moment later gets nothing.
   * Where a delivery has to wait for its recipient, correlate a message to that
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
   * Correlate a message for the workflow-aggregate's workflow or its sub-workflows
   * (call-activities).
   * <p>
   * The message's content is <i>never</i> transmitted to the BPMS: the
   * workflow aggregate is the single source of truth. BPMN logic
   * (expressions, conditions) reads data from the workflow aggregate, not
   * from message payloads. Incorporate any data of the incoming message into
   * the workflow aggregate before calling this method.
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
   * Correlate a message for the workflow-aggregate's workflow or its sub-workflows
   * (call-activities).
   * <p>
   * The message's content is <i>never</i> transmitted to the BPMS: the
   * workflow aggregate is the single source of truth. BPMN logic
   * (expressions, conditions) reads data from the workflow aggregate, not
   * from message payloads. Incorporate any data of the incoming message into
   * the workflow aggregate before calling this method.
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
   * Complete a user-task
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
   * Complete a user-task by sending a BPMN error
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param taskId        The task-id reported previously
   * @param bpmnErrorCode The error code which can be caught in BPMN by error
   *                      boundary events
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A cancelUserTask(
      A workflowAggregate,
      String taskId,
      String bpmnErrorCode);

  /**
   * Complete an asynchronous task
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
   * Complete an asynchronous task by sending a BPMN error
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param taskId        The task-id reported previously
   * @param bpmnErrorCode The error code which can be caught in BPMN by error
   *                      boundary events
   * @return The persisted workflow-aggregate, attached where the persistence layer
   *         works that way (e.g. JPA)
   * @see TaskId
   */
  A cancelTask(
      A workflowAggregate,
      String taskId,
      String bpmnErrorCode);

  /**
   * The <a href="https://github.com/vanillabp/adapter-platform-integration/wiki/Workflow-modules" target="_blank">workflow-module</a>
   * ID this process service belongs to.
   *
   * @return The workflow-module ID
   */
  String getWorkflowModuleId();

  /**
   * Get all process definitions of workflows affected by this service.
   *
   * @param workflowAggregate The workflow-aggregate for which to get the process definitions
   * @param historyContext Null for the primary process of the workflow or a value from
   *                       {@link WorkflowElementHistory#secondaryWorkflowHistoryContext()}
   *                       for secondary processes of the workflow (call activities)
   * @return The process definitions
   * @throws WorkflowNotFoundException If the workflow-aggregate is not associated with a workflow
   */
  default List<ProcessDefinition> getProcessDefinitions(
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException {
    throw new UnsupportedOperationException(
        "getProcessDefinitions is implemented by a VanillaBP adapter");
  }

  /**
   * Get the BPMN XML for a process definition.
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
   * Get the workflow history for the workflow associated with the given aggregate.
   *
   * @param workflowAggregate The workflow-aggregate for which to get the history
   * @param historyContext Null for the primary process of the workflow or a value from
   *                       {@link WorkflowElementHistory#secondaryWorkflowHistoryContext()}
   *                       for secondary processes of the workflow (call activities)
   * @return The workflow history
   * @throws WorkflowNotFoundException If the workflow-aggregate is not associated with a workflow
   */
  default WorkflowHistory getWorkflowHistory(
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException {
    throw new UnsupportedOperationException(
        "getWorkflowHistory is implemented by a VanillaBP adapter");
  }

}
