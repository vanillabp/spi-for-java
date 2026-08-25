package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a method for processing a certain
 * process-task (e.g. service-task, send-task, etc.):
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "doSomeWorkload")
 * public void doSomeWorkload(final MyWorkflowAggregate aggregate) throws {@link TaskException} {
 * </pre>
 * <p>
 * What the version range names, and why a delivery without a reported version is served only by a
 * method without one, is decision 5 in the repository's DECISIONS.md.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
@Repeatable(WorkflowTasks.class)
public @interface WorkflowTask {

  static String USE_METHOD_NAME = "";

  /**
   * @return The activity's BPMN id. Defaults to the annotated method's name.
   */
  String id() default USE_METHOD_NAME;

  /**
   * @return The task-definition as defined in the BPMN. Defaults to the annotated
   *         method's name.
   */
  String taskDefinition() default USE_METHOD_NAME;

  /**
   * Which versions of the deployed BPMN process this method serves. The version is
   * the version of the process DEFINITION as the BPMS counts it (Camunda 7 and
   * Camunda 8 count integers upwards per BPMN process id), not a version the
   * application invents.
   * <p>
   * A boundary is either such a version or a version TAG given in the model
   * (<code>camunda:versionTag</code> in Camunda 7, <code>zeebe:versionTag</code> in
   * Camunda 8):
   * <ul>
   * <li><i>*</i>: every version (the default)
   * <li><i>3</i> or <i>release-2024</i>: exactly that version, respectively every
   * version carrying that tag
   * <li><i>1-3</i> or <i>v1.0..v2.0</i>: a range, both boundaries included
   * <li><i>&gt;3</i>, <i>&lt;v2.0</i>: open ended</li>
   * </ul>
   * Ranges accept <code>..</code> as well as <code>-</code> as their separator; a
   * boundary naming a tag which contains a <code>-</code> has to use <code>..</code>.
   * &quot;Greater&quot; and &quot;less&quot; mean the deployment order, which for a
   * BPMS counting versions upwards is the numeric order.
   * <p>
   * Several methods may serve one BPMN element as long as their versions do not
   * overlap - overlapping specifications are reported when the application starts.
   * A BPMS which does not report the version of a process serves every method
   * regardless of this attribute, and specifications naming a version tag need a
   * BPMS which can be asked about its tags (Camunda 8 needs its query API for that).
   *
   * @return The versions of the deployed BPMN process this method serves.
   */
  String[] version() default "*";

}
