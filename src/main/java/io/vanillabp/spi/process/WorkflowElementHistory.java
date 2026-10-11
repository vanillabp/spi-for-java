package io.vanillabp.spi.process;

import java.time.OffsetDateTime;

/**
 * One step a workflow took, as part of {@link WorkflowHistory#elementsHistory()}.
 * <p>
 * Which steps a BPMS records depends on the BPMS and its configuration. Some keep only the
 * activities. Others keep every gateway and sequence flow as well. So a viewer colours what it is
 * told about and leaves the rest as it is.
 *
 * @param startTime StartTime of the workflow element
 * @param endTime EndTime of the workflow element (if completed or canceled)
 * @param elementId The id of the workflow element
 * @param elementType The type of the workflow element. A BPMS which records no types in the history
 *                    of its elements reports UNKNOWN
 * @param error An error message if the workflow element is in error right now
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
