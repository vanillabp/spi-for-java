package io.vanillabp.spi.process;

import java.time.OffsetDateTime;

/**
 * @param startTime StartTime of the workflow element
 * @param endTime EndTime of the workflow element (if completed or canceled)
 * @param elementId The id of the workflow element
 * @param elementType The type of the workflow element - UNKNOWN is used for BPMS not supporting types in history of
 *                    elements
 * @param error An error message if the workflow element is currently in error
 * @param isCanceled Whether the workflow element was canceled
 * @param secondaryWorkflowHistoryContext The secondary workflow history context if the element is a call activity
 */
public record WorkflowElementHistory(
                                     OffsetDateTime startTime,
                                     OffsetDateTime endTime,
                                     String elementId,
                                     WorkflowElementType elementType,
                                     String error,
                                     boolean isCanceled,
                                     String secondaryWorkflowHistoryContext) {
}
