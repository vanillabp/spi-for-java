package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method which builds the workflow aggregate of a workflow that VanillaBP did not
 * start. For example a timer, signal or conditional start event fired, or somebody with
 * access to the BPMS started the process directly.
 * <p>
 * The method NAMES the workflow. The id of a workflow is the id of its workflow
 * aggregate. The application assigns it, and the BPMS is told about it afterwards.
 * Camunda 7 keeps it as the business key. Camunda 8 and the Process-Engine-API keep it as
 * a process variable named after the id attribute of the aggregate. So the aggregate this
 * method returns has to carry its id, unless the persistence layer assigns one on save.
 * See decision 9 in the repository's DECISIONS.md for why the application names the workflow.
 *
 * <pre>
 * &#64;WorkflowStartedByBpms
 * public Ride buildAggregate(final {@link BpmsStartTrigger} trigger, &#64;{@link TaskParam}("startedAt") final Instant startedAt) {
 *   return new Ride(UUID.randomUUID().toString(), startedAt);
 * }
 * </pre>
 *
 * The method may take a {@link BpmsStartTrigger} and process variables annotated with
 * {@link TaskParam}, in any order. It returns the workflow aggregate. It runs in the
 * transaction VanillaBP opened for the start, and the aggregate is saved afterwards. If
 * the method throws, the workflow does not start. The aggregate is rolled back, and the
 * BPMS retries the way it always does.
 * <p>
 * A process which is never started past VanillaBP needs no such method. If one is started
 * that way anyway, the start is refused with a message which shows the method to write.
 * The BPMS turns that into an incident, as it does for any failing task.
 * <p>
 * A workflow the application started through
 * {@link io.vanillabp.spi.process.ProcessService#startWorkflow(Object)} never reaches this
 * method, because it already has its id and its workflow aggregate. The start event of an
 * event subprocess does not reach it either, because it fires inside a workflow which is
 * already running.
 * <p>
 * See decision 8 in the repository's DECISIONS.md. It says what the version range names. It
 * says why a delivery without a reported version is served only by a method without a range.
 * And it says how a method which names no range takes the range of its {@link BpmnProcess}.
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
   * Which start event this method is interested in. Name one if a process has several start
   * events, and an aggregate built for a timer differs from one built for a signal.
   *
   * @return The BPMN id of the start event this method serves. Defaults to every start
   *         event of the process. That is what a process with one start event needs.
   */
  String id() default ANY_START_EVENT;

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
   * A delivery whose version the BPMS does not report is served only by a method
   * without a range. Such a method names no version, and the {@link BpmnProcess} it
   * was declared with names none either. A range which names a version tag needs a BPMS
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
