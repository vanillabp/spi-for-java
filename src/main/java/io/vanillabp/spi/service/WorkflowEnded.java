package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method which is called once a workflow has ended. Without it, an
 * application learns about the end only from a service task in front of every end
 * event. That clutters the BPMN model with a fact the engine knows anyway.
 *
 * <pre>
 * &#64;WorkflowEnded
 * public void rideFinished(final Ride ride, final {@link WorkflowEnd} end) {
 *   ride.setClosedAt(end.time());
 * }
 * </pre>
 *
 * The method may take the workflow aggregate and a {@link WorkflowEnd} in any
 * order. VanillaBP loads the aggregate, calls the method and saves the aggregate.
 * So the method only has to record the outcome.
 * <p>
 * The annotation is OPTIONAL, and a model without it costs nothing. Adapters add
 * their listener only where such a method exists.
 * <p>
 * <b>The notification arrives at least once.</b> After a crash or a retried
 * delivery it may arrive twice. So write the method in a way that running it twice
 * does no harm (idempotent). Recording a status is such a way. Whether it runs in
 * the transaction which ends the workflow depends on the BPMS. An embedded engine
 * ends the workflow and calls the method in ONE transaction. A remote BPMS sends
 * the notification afterwards.
 * <p>
 * See decision 8 in the repository's DECISIONS.md. It says what the version range names. It
 * says why a delivery without a reported version is served only by a method without a range.
 * And it says how a method which names no range takes the range of its {@link BpmnProcess}.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
public @interface WorkflowEnded {

  /**
   * The default of {@link #id()}: the method is called whichever end the workflow took.
   */
  static String ANY_END_EVENT = "";

  /**
   * Which end event this method is interested in. Name one if a workflow can end in several
   * places, and the end it reached tells the outcome.
   *
   * @return The BPMN id of the end event this method serves. Defaults to every end
   *         of the workflow. That is what a process with one end needs, and it is
   *         the only thing a BPMS which reports no element id can serve.
   */
  String id() default ANY_END_EVENT;

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
