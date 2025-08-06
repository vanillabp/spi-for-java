package io.vanillabp.spi.process;

import io.vanillabp.spi.service.TaskId;
import java.io.InputStream;
import java.util.List;

/**
 * @param <DE> The workflow-aggregate-class
 */
public interface ProcessService<DE> {

    /**
     * Start a new workflow.
     * 
     * @param workflowAggregate The workflow-aggregate
     * @return The workflow-aggregate attached to JPA
     */
    DE startWorkflow(
        DE workflowAggregate);

  /**
   * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
   * (call-activities).
   *
   * @param workflowAggregate The workflow-aggregate
   * @param messageName  The message name to be correlated
   * @return The workflow-aggregate attached to JPA
   */
  DE correlateMessage(
      DE workflowAggregate,
      String messageName);

  /**
   * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
   * (call-activities).
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param messageName   The message name to be correlated
   * @param correlationId The correlation-id
   * @return The workflow-aggregate attached to JPA
   */
  DE correlateMessage(
      DE workflowAggregate,
      String messageName,
      String correlationId);

  /**
   * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
   * (call-activities).
   *
   * @param workflowAggregate The workflow-aggregate
   * @param message      The message to correlate. The object's class simple name
   *                     is used as the message name.
   * @return The workflow-aggregate attached to JPA
   */
  DE correlateMessage(
      DE workflowAggregate,
      Object message);

  /**
   * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
   * (call-activities).
   *
   * @param workflowAggregate  The workflow-aggregate
   * @param message       The message to correlate. The object's class simple name
   *                      is used as the message name.
   * @param correlationId The correlation-id
   * @return The workflow-aggregate attached to JPA
   */
  DE correlateMessage(
      DE workflowAggregate,
      Object message,
      String correlationId);

  /**
   * Complete a user-task
   *
   * @param workflowAggregate The workflow-aggregate
   * @param taskId       The task-id reported previously
   * @return The workflow-aggregate attached to JPA
   * @see TaskId
   */
  DE completeUserTask(
      DE workflowAggregate,
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
  DE cancelUserTask(
      DE workflowAggregate,
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
  DE completeTask(
      DE workflowAggregate,
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
  DE cancelTask(
      DE workflowAggregate,
      String taskId,
      String bpmnErrorCode);

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
  List<ProcessDefinition> getProcessDefinitions(
      DE workflowAggregate,
      String historyContext) throws WorkflowNotFoundException;

  /**
   * Get the BPMN XML for a process definition.
   *
   * @param processDefinitionId The process definition id
   * @return The BPMN XML as an input stream
   * @throws ProcessDefinitionNotFoundException If the process definition is not found
   */
  InputStream getBpmnXml(
      String processDefinitionId) throws ProcessDefinitionNotFoundException;

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
  WorkflowHistory getWorkflowHistory(
      DE workflowAggregate,
      String historyContext) throws WorkflowNotFoundException;

}
