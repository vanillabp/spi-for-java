package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to wire workflow-services to the processes they are responsible for.
 * <p>
 * Where the state of a workflow lives, and why that is the aggregate rather than a process
 * variable, is decision 1 in the repository's DECISIONS.md.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface WorkflowService {

  /**
   * @return The workflow-aggregate associated with the annotated service
   */
  Class<?> workflowAggregateClass();

  /**
   * @return The process definition id as defined in the BPMN for which the
   *         annotated service is responsible for. Defaults to the bean name of
   *         the service. This id is used to start new workflows and
   *         correlate messages not related to a dedicated workflow.
   */
  BpmnProcess bpmnProcess() default @BpmnProcess(bpmnProcessId = BpmnProcess.USE_CLASS_NAME);

  /**
   * Any further BPMN processes the annotated service is responsible for. A process called by
   * a call activity of the primary one is the common case, and a process which was RENAMED is
   * the second.
   * <p>
   * Renaming a BPMN process is the one refactoring which reaches into the BPMS: the old id
   * stays there with every version ever deployed under it and with the workflows still
   * running on them, while the software brings only the new name. Naming the old id here,
   * with the {@link BpmnProcess#version()} of the versions the BPMS still holds under it,
   * keeps those workflows served by the methods of this class:
   *
   * <pre>
   * &#64;WorkflowService(
   *         workflowAggregateClass = OrderApproval.class,
   *         bpmnProcess = &#64;BpmnProcess(bpmnProcessId = "OrderApproval"),
   *         secondaryBpmnProcesses = &#64;BpmnProcess(bpmnProcessId = "order_approval", version = "1-3"))
   * </pre>
   *
   * Each entry is a declaration of its own and carries its own version range. Mind what that
   * means for a rename: a BPMS counts the versions of each process id separately, so version 2
   * of the old id and version 2 of the new one are different models, and a range naming numbers
   * belongs to the declaration it stands on rather than to the class.
   * <p>
   * A workflow is STARTED under {@link #bpmnProcess()} only. A secondary process serves what is
   * already running, and the workflows of the old id end under it.
   *
   * @return The further BPMN process definition ids the annotated service is responsible for
   */
  BpmnProcess[] secondaryBpmnProcesses() default {};
}
