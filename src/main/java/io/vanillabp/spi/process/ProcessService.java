package io.vanillabp.spi.process;

import java.io.InputStream;
import java.util.List;

import io.vanillabp.spi.service.TaskId;

/**
 * @param <A> The workflow-aggregate-class
 */
public interface ProcessService<A> {

  /**
   * Start a new workflow.
   *
   * @param workflowAggregate The workflow-aggregate
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
   */
  default A startWorkflowByMessage(
      A workflowAggregate,
      String messageName) {
    throw new UnsupportedOperationException(
        "startWorkflowByMessage is implemented by a VanillaBP adapter");
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
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
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
   * @return The workflow-aggregate attached to JPA
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
