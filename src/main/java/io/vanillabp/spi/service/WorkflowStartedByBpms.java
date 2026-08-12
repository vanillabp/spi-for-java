package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method building the workflow-aggregate of a workflow the BPMS started
 * on its own: by a timer, a signal or a conditional start event. Unlike every other
 * workflow, nobody called
 * {@link io.vanillabp.spi.process.ProcessService#startWorkflow(Object)} - the engine
 * decided, and the aggregate has to come into existence for the workflow to have
 * any data at all.
 * <p>
 * The annotation is OPTIONAL. Without it VanillaBP builds the aggregate itself: it
 * instantiates the class, assigns an ID (a timer's trigger time, otherwise a
 * generated ID) and copies the process variables the BPMN model set into
 * equally-named attributes. Annotate a method to take that over:
 *
 * <pre>
 * &#64;WorkflowStartedByBpms
 * public Ride buildAggregate(final {@link BpmsStartTrigger} trigger) {
 *   return new Ride(trigger.time());
 * }
 * </pre>
 *
 * or to enrich the aggregate VanillaBP built:
 *
 * <pre>
 * &#64;WorkflowStartedByBpms(id = "DailySettlementTimer")
 * public void enrich(final Settlement settlement, &#64;{@link TaskParam}("region") final String region) {
 *   settlement.setRegion(region);
 * }
 * </pre>
 *
 * The method may take the workflow aggregate, a {@link BpmsStartTrigger} and
 * {@link TaskParam} annotated process variables in any order. It runs in the
 * transaction VanillaBP opened for the start; the aggregate is saved afterwards.
 * Throwing means the workflow does not start: the aggregate is rolled back and the
 * BPMS applies its retry semantics.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
public @interface WorkflowStartedByBpms {

  static String ANY_START_EVENT = "";

  /**
   * @return The BPMN id of the start event this method serves. Defaults to every
   *         BPMS-initiated start event of the process - which is what a process
   *         with exactly one such start event needs.
   */
  String id() default ANY_START_EVENT;

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
