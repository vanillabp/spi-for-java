package io.vanillabp.spi.process;

import java.util.List;

/**
 * @param id The process definition id (opaque string)
 * @param bpmnProcessId The bpmn process id
 * @param version The version of the process (opaque string)
 * @param usedByElements null, if the process definition is the primary process definition of the workflow
 *                       otherwise a list of element ids that use the process definition (call-activities)
 */
public record ProcessDefinition(
                                String id,
                                String bpmnProcessId,
                                String version,
                                List<String> usedByElements) {
}
