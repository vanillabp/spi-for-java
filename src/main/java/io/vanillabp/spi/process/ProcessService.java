package io.vanillabp.spi.process;

import io.vanillabp.spi.service.TaskId;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public interface ProcessService<DE> {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The domain-entity
     * @return The domain-entity attached to JPA
     */
    DE startWorkflow(DE domainEntity) throws Exception;

    /**
     * @return The BPMN process-id this service belongs to
     */
    String getBpmnProcessId();

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param domainEntity The domain-entity
     * @param messageName  The message name to be correlated
     * @return The domain-entity attached to JPA
     */
    DE correlateMessage(DE domainEntity, String messageName);

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param domainEntity  The domain-entity
     * @param messageName   The message name to be correlated
     * @param correlationId The correlation-id
     * @return The domain-entity attached to JPA
     */
    DE correlateMessage(DE domainEntity, String messageName, String correlationId);

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param domainEntity The domain-entity
     * @param message      The message to correlate. The object's class simple name
     *                     is used as the message name.
     * @return The domain-entity attached to JPA
     */
    DE correlateMessage(DE domainEntity, Object message);

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param domainEntity  The domain-entity
     * @param message       The message to correlate. The object's class simple name
     *                      is used as the message name.
     * @param correlationId The correlation-id
     * @return The domain-entity attached to JPA
     */
    DE correlateMessage(DE domainEntity, Object message, String correlationId);

    /**
     * Complete a user-task
     * 
     * @param domainEntity The domain-entity
     * @param taskId       The task-id reported previously
     * @return The domain-entity attached to JPA
     * @see TaskId
     */
    DE completeUserTask(DE domainEntity, String taskId);

    /**
     * Complete a user-task by sending a BPMN error
     * 
     * @param domainEntity  The domain-entity
     * @param taskId        The task-id reported previously
     * @param bpmnErrorCode The error code which can be caught in BPMN by error
     *                      boundary events
     * @return The domain-entity attached to JPA
     * @see TaskId
     */
    DE cancelUserTask(DE domainEntity, String taskId, String bpmnErrorCode);

    /**
     * Complete an asynchronous task
     * 
     * @param domainEntity The domain-entity
     * @param taskId       The task-id reported previously
     * @return The domain-entity attached to JPA
     * @see TaskId
     */
    DE completeTask(DE domainEntity, String taskId);

    /**
     * Complete an asynchronous task by sending a BPMN error
     * 
     * @param domainEntity  The domain-entity
     * @param taskId        The task-id reported previously
     * @param bpmnErrorCode The error code which can be caught in BPMN by error
     *                      boundary events
     * @return The domain-entity attached to JPA
     * @see TaskId
     */
    DE cancelTask(DE domainEntity, String taskId, String bpmnErrorCode);

}
