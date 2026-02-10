package io.vanillabp.spi.process;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @param processDefinitionId The process definition id (opaque string)
 * @param startTime Timestamp of the workflow start
 * @param endTime Timestamp of the workflow end (if completed or canceled)
 * @param elementsHistory History of workflow elements processed or null if not supported by the underlying BPMS
 */
public record WorkflowHistory(
                              String processDefinitionId,
                              OffsetDateTime startTime,
                              OffsetDateTime endTime,
                              List<WorkflowElementHistory> elementsHistory) {
}
