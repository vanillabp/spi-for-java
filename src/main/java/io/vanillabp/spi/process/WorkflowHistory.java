package io.vanillabp.spi.process;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * What a BPMS remembers about one workflow, as reported by
 * {@link ProcessService#getWorkflowHistory(Object, String)}.
 * <p>
 * A BPMN viewer uses it to colour the model. The model comes from
 * {@link ProcessService#getBpmnXml(String)}. {@link #elementsHistory()} says which of its elements
 * the workflow has passed, and how.
 *
 * @param processDefinitionId The process definition id (opaque string)
 * @param startTime Timestamp of the workflow start
 * @param endTime Timestamp of the workflow end (if completed or canceled)
 * @param elementsHistory History of the workflow elements processed, or null if the BPMS does not support it
 */
public record WorkflowHistory(
                              String processDefinitionId,
                              OffsetDateTime startTime,
                              OffsetDateTime endTime,
                              List<WorkflowElementHistory> elementsHistory) {
}
