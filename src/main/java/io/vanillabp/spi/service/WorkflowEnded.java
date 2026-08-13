package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method to be called once a workflow ended. Without it an application
 * learns of the end only by modelling a service task in front of every end event -
 * BPMN noise for a fact the engine knows anyway.
 *
 * <pre>
 * &#64;WorkflowEnded
 * public void rideFinished(final Ride ride, final {@link WorkflowEnd} end) {
 *   ride.setClosedAt(end.time());
 * }
 * </pre>
 *
 * The method may take the workflow aggregate and a {@link WorkflowEnd} in any
 * order. VanillaBP loads the aggregate, calls the method and saves the aggregate,
 * so recording the outcome is all the method has to do.
 * <p>
 * The annotation is OPTIONAL, and a model without it pays nothing: adapters attach
 * their listener only where a method exists.
 * <p>
 * <b>The notification is at-least-once</b> - after a crash or a retried delivery it
 * may arrive twice, so write it idempotently (which recording a status is). Whether
 * it runs in the transaction ending the workflow depends on the BPMS: an embedded
 * engine ends the workflow and calls the method in ONE transaction, a remote BPMS
 * delivers the notification afterwards.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
public @interface WorkflowEnded {

  static String ANY_END_EVENT = "";

  /**
   * @return The BPMN id of the end event this method serves. Defaults to every end
   *         of the workflow, which is what a process ending in one place needs -
   *         and the only thing a BPMS reporting no element id can serve.
   */
  String id() default ANY_END_EVENT;

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
