package io.vanillabp.spi.process;

import java.util.List;

/**
 * One of the BPMN process definitions a workflow runs on, as reported by
 * {@link ProcessService#getProcessDefinitions(Object, String)}.
 * <p>
 * A workflow which calls other processes runs on more than one definition, so that method returns
 * a list. It holds the process the workflow was started with, plus the process behind each of its
 * call activities. Pass {@link #id()} to {@link ProcessService#getBpmnXml(String)} to get the model
 * itself, for example to draw it in a BPMN viewer.
 *
 * @param id The process definition id (opaque string)
 * @param bpmnProcessId The bpmn process id
 * @param version The version of the process (opaque string)
 * @param usedByElements Null for the primary process definition of the workflow. For any other
 *                       one, the ids of the call activities which use it
 */
public record ProcessDefinition(
                                String id,
                                String bpmnProcessId,
                                String version,
                                List<String> usedByElements) {
}
