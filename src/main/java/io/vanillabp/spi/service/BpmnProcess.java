package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to wire workflow-services to the processes they are responsible for.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface BpmnProcess {

  String ALL_VERSIONS = "*";

  String USE_CLASS_NAME = "";

  /**
   * @return The process-id as defined in the BPMN for which the annotated service
   *         is responsible for. Defaults to the bean name of the service.
   */
  String bpmnProcessId() default BpmnProcess.USE_CLASS_NAME;

  /**
   * Which versions of this deployed BPMN process the annotated class serves. It is the
   * FALLBACK of {@link WorkflowTask#version()}, {@link WorkflowStartedByBpms#version()}
   * and {@link WorkflowEnded#version()}: a method of this class naming no version of its
   * own serves the range declared here, so a class holding the handlers of one generation
   * of a model states that once instead of once per method. A method naming its own range
   * keeps it word by word - the range declared here does not narrow it.
   * <p>
   * The version is the version of the process DEFINITION as the BPMS counts it (Camunda 7
   * and Camunda 8 count integers upwards per BPMN process id), not a version the
   * application invents. A boundary is either such a version or a version TAG given in
   * the model (<code>camunda:versionTag</code> in Camunda 7, <code>zeebe:versionTag</code>
   * in Camunda 8):
   * <ul>
   * <li><i>*</i>: every version (the default)
   * <li><i>3</i> or <i>release-2024</i>: exactly that version, respectively every version
   * carrying that tag
   * <li><i>1-3</i> or <i>v1.0..v2.0</i>: a range, both boundaries included
   * <li><i>&gt;3</i>, <i>&gt;=3</i>, <i>&lt;v2.0</i>, <i>&lt;=v2.0</i>: open ended, the
   * boundary excluded respectively included</li>
   * </ul>
   * Ranges accept <code>..</code> as well as <code>-</code> as their separator; a boundary
   * naming a tag which contains a <code>-</code> has to use <code>..</code>.
   * &quot;Greater&quot; and &quot;less&quot; mean the deployment order, which for a BPMS
   * counting versions upwards is the numeric order.
   * <p>
   * Two classes may declare the same BPMN process, which is what this attribute is for -
   * one per generation of the model. Their ranges must not overlap; overlapping ones are
   * reported when the application starts. Every declaration carries its own version, the
   * entries of <code>secondaryBpmnProcesses</code> included, and a method serving elements
   * of two processes inherits per process.
   * <p>
   * A method which INHERITS a range is as restricted as one naming it, so it does not serve
   * a delivery whose version the BPMS did not report - see decision 8 in the repository's
   * DECISIONS.md.
   *
   * @return The versions of this deployed BPMN process the annotated class serves
   */
  String[] version() default ALL_VERSIONS;

}
