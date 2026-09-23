package io.vanillabp.spi.process;

import java.util.List;

/**
 * One of the BPMN process definitions a workflow runs on, as reported by
 * {@link ProcessService#getProcessDefinitions(Object, String)}.
 * <p>
 * A workflow which calls other processes runs on more than one of them, which is why that method
 * answers with a list: the process the workflow was started with, plus the process behind every
 * call activity of it. Hand {@link #id()} to {@link ProcessService#getBpmnXml(String)} to get the
 * model itself, for example to draw it in a BPMN viewer.
 *
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
