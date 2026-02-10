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
   * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
   * (call-activities).
   *
   * @param workflowAggregate The workflow-aggregate
   * @param messageName  The message name to be correlated
   * @return The workflow-aggregate attached to JPA
   */
  A correlateMessage(
      A workflowAggregate,
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
  A correlateMessage(
      A workflowAggregate,
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
  A correlateMessage(
      A workflowAggregate,
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
  A correlateMessage(
      A workflowAggregate,
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
   * The <a href="https://github.com/vanillabp/adapter-platform-integration/wiki/Workflow-modules" target="_blank"></a>workflow-module</a>
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
  List<ProcessDefinition> getProcessDefinitions(
      A workflowAggregate,
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
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException;

}
