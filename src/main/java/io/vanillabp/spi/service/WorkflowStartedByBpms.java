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
