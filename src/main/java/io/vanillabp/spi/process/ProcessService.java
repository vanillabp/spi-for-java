package io.vanillabp.spi.process;

import io.vanillabp.spi.service.TaskId;

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
    DE startWorkflow(DE workflowAggregate) throws Exception;

    /**
     * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param workflowAggregate The workflow-aggregate
     * @param messageName  The message name to be correlated
     * @return The workflow-aggregate attached to JPA
     */
    DE correlateMessage(DE workflowAggregate, String messageName);

    /**
     * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param workflowAggregate  The workflow-aggregate
     * @param messageName   The message name to be correlated
     * @param correlationId The correlation-id
     * @return The workflow-aggregate attached to JPA
     */
    DE correlateMessage(DE workflowAggregate, String messageName, String correlationId);

    /**
     * Correlate a message for the workflow-aggregate's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param workflowAggregate The workflow-aggregate
     * @param message      The message to correlate. The object's class simple name
     *                     is used as the message name.
     * @return The workflow-aggregate attached to JPA
     */
    DE correlateMessage(DE workflowAggregate, Object message);

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
    DE correlateMessage(DE workflowAggregate, Object message, String correlationId);

    /**
     * Complete a user-task
     * 
     * @param workflowAggregate The workflow-aggregate
     * @param taskId       The task-id reported previously
     * @return The workflow-aggregate attached to JPA
     * @see TaskId
     */
    DE completeUserTask(DE workflowAggregate, String taskId);

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
    DE cancelUserTask(DE workflowAggregate, String taskId, String bpmnErrorCode);

    /**
     * Complete an asynchronous task
     * 
     * @param workflowAggregate The workflow-aggregate
     * @param taskId            The task-id reported previously
     * @return The workflow-aggregate attached to JPA
     * @see TaskId
     */
    DE completeTask(DE workflowAggregate, String taskId);

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
    DE cancelTask(DE workflowAggregate, String taskId, String bpmnErrorCode);

}
