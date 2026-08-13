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
   * Can be used to define certain versions or ranges of versions of a process for
   * which the annotated method should be used for.
   * <p>
   * Format:
   * <ul>
   * <li><i>*</i>: all versions
   * <li><i>1</i>: only version &quot;1&quot;
   * <li><i>1-3</i>: only versions &quot;1&quot;, &quot;2&quot; and &quot;3&quot;
   * <li><i>&gt;3</i>: only versions higher than &quot;3&quot;
   * <li><i>&lt;3</i>: only versions less than &quot;3&quot;</li>
   * </ul>
   *
   * @return The version of the process this method belongs to.
   */
  String[] version() default "*";

}
