package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Connects a workflow service to the processes it is responsible for.
 * <p>
 * See decision 1 in the repository's DECISIONS.md. It says where the state of a workflow lives,
 * and why that is the aggregate and not a process variable.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface WorkflowService {

  /**
   * The class of the workflow aggregate this service works on. It decides which persistence
   * VanillaBP uses to load and save the aggregate. It is also the type of the aggregate parameter
   * of the methods of this class.
   *
   * @return The workflow-aggregate associated with the annotated service
   */
  Class<?> workflowAggregateClass();

  /**
   * The process this service is connected to. It is the only one a workflow is started under.
   * Its {@link BpmnProcess#version()} is the fallback for every method of this class which names
   * no version of its own.
   *
   * @return The process definition id as defined in the BPMN, which the annotated
   *         service is responsible for. Defaults to the simple name of the
   *         annotated class. This id is used to start new workflows and to
   *         correlate messages which belong to no particular workflow.
   */
  BpmnProcess bpmnProcess() default @BpmnProcess(bpmnProcessId = BpmnProcess.USE_CLASS_NAME);

  /**
   * Any further BPMN processes the annotated service is responsible for. The common case is a
   * process called by a call activity of the primary one. The second case is a process which
   * was RENAMED.
   * <p>
   * Renaming a BPMN process is the one refactoring which reaches into the BPMS. The old id stays
   * there, with every version ever deployed under it and with the workflows still running on
   * them. But the software only brings the new name. Name the old id here, with the
   * {@link BpmnProcess#version()} of the versions the BPMS still holds under it. Then the methods
   * of this class keep serving those workflows:
   *
   * <pre>
   * &#64;WorkflowService(
   *         workflowAggregateClass = OrderApproval.class,
   *         bpmnProcess = &#64;BpmnProcess(bpmnProcessId = "OrderApproval"),
   *         secondaryBpmnProcesses = &#64;BpmnProcess(bpmnProcessId = "order_approval", version = "1-3"))
   * </pre>
   *
   * Each entry is a declaration of its own and has its own version range. Keep in mind what
   * this means for a rename. A BPMS counts the versions of each process id separately. So
   * version 2 of the old id and version 2 of the new one are different models. A range of
   * numbers belongs to the declaration it is written on, not to the class.
   * <p>
   * A workflow is STARTED under {@link #bpmnProcess()} only. A secondary process serves what is
   * already running, and the workflows of the old id end under it.
   *
   * @return The further BPMN process definition ids the annotated service is responsible for
   */
  BpmnProcess[] secondaryBpmnProcesses() default {};
}
