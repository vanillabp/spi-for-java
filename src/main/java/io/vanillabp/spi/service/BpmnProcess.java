package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Connects a workflow service to the BPMN process it is responsible for.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface BpmnProcess {

  /**
   * The default of {@link #version()}: every version of the process the BPMS holds. This includes
   * versions deployed after this class was written.
   */
  String ALL_VERSIONS = "*";

  /**
   * The default of {@link #bpmnProcessId()}: the annotation names no process. So the name of the
   * annotated service decides which process it serves.
   */
  String USE_CLASS_NAME = "";

  /**
   * Which BPMN process this service serves. It is named by the {@code id} attribute of the process
   * in the BPMN file, not by its name.
   *
   * @return The process-id as defined in the BPMN, which the annotated service is responsible
   *         for. Defaults to the simple name of the annotated class.
   */
  String bpmnProcessId() default BpmnProcess.USE_CLASS_NAME;

  /**
   * Which versions of this deployed BPMN process the annotated class serves. It is the
   * FALLBACK of {@link WorkflowTask#version()}, {@link WorkflowStartedByBpms#version()}
   * and {@link WorkflowEnded#version()}. A method of this class which names no version of
   * its own serves the range declared here. So a class with the handlers of one generation
   * of a model says it once, not once per method. A method which names its own range keeps
   * it exactly as written. The range declared here does not narrow it.
   * <p>
   * The version is the version of the process DEFINITION as the BPMS counts it. Camunda 7
   * and Camunda 8 count integers upwards per BPMN process id. It is not a version the
   * application makes up. A boundary is either such a version or a version TAG given in
   * the model (<code>camunda:versionTag</code> in Camunda 7, <code>zeebe:versionTag</code>
   * in Camunda 8):
   * <ul>
   * <li><i>*</i>: every version (the default)
   * <li><i>3</i> or <i>release-2024</i>: exactly that version, or every version with that
   * tag
   * <li><i>1-3</i> or <i>v1.0..v2.0</i>: a range, both boundaries included
   * <li><i>&gt;3</i>, <i>&gt;=3</i>, <i>&lt;v2.0</i>, <i>&lt;=v2.0</i>: open ended. With
   * <i>=</i> the boundary is included, without it the boundary is excluded</li>
   * </ul>
   * Ranges accept <code>..</code> as well as <code>-</code> as their separator. If a
   * boundary names a tag which contains a <code>-</code>, use <code>..</code>.
   * &quot;Greater&quot; and &quot;less&quot; mean the order of deployment. For a BPMS
   * which counts versions upwards, that is the numeric order.
   * <p>
   * Two classes may declare the same BPMN process, one per generation of the model. That
   * is what this attribute is for. Their ranges must not overlap. Overlapping ranges are
   * reported when the application starts. Every declaration carries its own version, also
   * the entries of <code>secondaryBpmnProcesses</code>. A method which serves elements of
   * two processes inherits the range of each process separately.
   * <p>
   * A method which INHERITS a range is as restricted as a method which names one. So it does
   * not serve a delivery whose version the BPMS did not report. See decision 8 in the
   * repository's DECISIONS.md.
   *
   * @return The versions of this deployed BPMN process the annotated class serves
   */
  String[] version() default ALL_VERSIONS;

}
