package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method which builds the workflow aggregate of a workflow that reached this
 * application without VanillaBP starting it: a timer, signal or conditional start event
 * fired, or somebody with access to the BPMS started the process directly.
 * <p>
 * The method NAMES the workflow. The id of a workflow is the id of its workflow
 * aggregate, the application assigns it, and the BPMS is told about it afterwards:
 * Camunda 7 keeps it as the business key, Camunda 8 and the Process-Engine-API keep it as
 * a process variable named after the aggregate's id attribute. So the aggregate this
 * method returns has to carry its id, unless the persistence layer assigns one on save.
 *
 * <pre>
 * &#64;WorkflowStartedByBpms
 * public Ride buildAggregate(final {@link BpmsStartTrigger} trigger, &#64;{@link TaskParam}("startedAt") final Instant startedAt) {
 *   return new Ride(UUID.randomUUID().toString(), startedAt);
 * }
 * </pre>
 *
 * The method may take a {@link BpmsStartTrigger} and {@link TaskParam} annotated process
 * variables in any order, and it returns the workflow aggregate. It runs in the
 * transaction VanillaBP opened for the start; the aggregate is saved afterwards. Throwing
 * means the workflow does not start: the aggregate is rolled back and the BPMS applies its
 * retry semantics.
 * <p>
 * A process nobody ever starts past VanillaBP needs no such method. Where one is started
 * all the same, the start is refused with a message showing the method to write, and the
 * BPMS turns that into an incident the way it does for any failing task.
 * <p>
 * A workflow the application started through
 * {@link io.vanillabp.spi.process.ProcessService#startWorkflow(Object)} never reaches this
 * method: it already carries its id and its workflow aggregate. Neither does a start event
 * of an event subprocess, which fires inside a workflow that is already running.
 * <p>
 * What the version range names, why a delivery without a reported version is served only by a
 * method without one, and how a method naming none takes the range of its
 * {@link BpmnProcess}, is decision 8 in the repository's DECISIONS.md.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
public @interface WorkflowStartedByBpms {

  /**
   * The default of {@link #id()}: the method is called for every start event of the process.
   */
  static String ANY_START_EVENT = "";

  /**
   * Which start event this method is interested in. Name one where a process has several of
   * them and an aggregate built for a timer differs from one built for a signal.
   *
   * @return The BPMN id of the start event this method serves. Defaults to every start
   *         event of the process, which is what a process with one start event needs.
   */
  String id() default ANY_START_EVENT;

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
   * <p>
   * Naming no version does not mean &quot;every version&quot; unconditionally: the method
   * then serves the range of the {@link BpmnProcess} its process was declared with, which
   * is how a whole workflow service class is bound to one generation of a model.
   *
   * @return The versions of the deployed BPMN process this method serves.
   */
  String[] version() default "*";

}
