package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method which does the work of a task in the process (e.g. a service
 * task or a send task):
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "doSomeWorkload")
 * public void doSomeWorkload(final MyWorkflowAggregate aggregate) throws {@link TaskException} {
 * </pre>
 * <p>
 * See decision 8 in the repository's DECISIONS.md. It says what the version range names. It
 * says why a delivery without a reported version is served only by a method without a range.
 * And it says how a method which names no range takes the range of its {@link BpmnProcess}.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
@Repeatable(WorkflowTasks.class)
public @interface WorkflowTask {

  /**
   * The default of {@link #id()} and of {@link #taskDefinition()}: the annotation names nothing.
   * So the model has to carry the name of the annotated method.
   */
  static String USE_METHOD_NAME = "";

  /**
   * Connects the method to the BPMN element by its id. Use it if you get a model whose task
   * definitions you may not change. An id is unique within the process anyway.
   *
   * @return The activity's BPMN id. Defaults to the annotated method's name.
   */
  String id() default USE_METHOD_NAME;

  /**
   * Connects the method to the BPMN element by the task definition in the properties of the
   * element. This is the usual way. A model written together with the software carries the name
   * of the method. For a model which existed first, name its task definition here.
   *
   * @return The task-definition as defined in the BPMN. Defaults to the annotated
   *         method's name.
   */
  String taskDefinition() default USE_METHOD_NAME;

  /**
   * Which versions of the deployed BPMN process this method serves. The version is
   * the version of the process DEFINITION as the BPMS counts it. Camunda 7 and
   * Camunda 8 count integers upwards per BPMN process id. It is not a version the
   * application makes up.
   * <p>
   * A boundary is either such a version or a version TAG given in the model
   * (<code>camunda:versionTag</code> in Camunda 7, <code>zeebe:versionTag</code> in
   * Camunda 8):
   * <ul>
   * <li><i>*</i>: every version (the default)
   * <li><i>3</i> or <i>release-2024</i>: exactly that version, or every version with
   * that tag
   * <li><i>1-3</i> or <i>v1.0..v2.0</i>: a range, both boundaries included
   * <li><i>&gt;3</i>, <i>&lt;v2.0</i>: open ended</li>
   * </ul>
   * Ranges accept <code>..</code> as well as <code>-</code> as their separator. If a
   * boundary names a tag which contains a <code>-</code>, use <code>..</code>.
   * &quot;Greater&quot; and &quot;less&quot; mean the order of deployment. For a BPMS
   * which counts versions upwards, that is the numeric order.
   * <p>
   * Several methods may serve one BPMN element if their versions do not overlap.
   * Overlapping ranges are reported when the application starts.
   * A BPMS which does not report the version of a process serves every method
   * regardless of this attribute. A range which names a version tag needs a BPMS
   * which can be asked about its tags. Camunda 8 needs its query API for that.
   * <p>
   * Naming no version does not simply mean &quot;every version&quot;. The method then
   * serves the range of the {@link BpmnProcess} its process was declared with. That is
   * how a whole workflow service class is bound to one generation of a model.
   *
   * @return The versions of the deployed BPMN process this method serves.
   */
  String[] version() default "*";

}
