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
 * The annotation is REQUIRED for a process the BPMS can start itself. VanillaBP does
 * not build the aggregate: an object which comes into existence without the
 * application does not carry the application's values, and for a BPMS-initiated start
 * that would be the very first thing that happens to the workflow. So the application
 * builds it and returns it:
 *
 * <pre>
 * &#64;WorkflowStartedByBpms
 * public Ride buildAggregate(final {@link BpmsStartTrigger} trigger) {
 *   return new Ride(trigger.time());
 * }
 * </pre>
 *
 * A process with a timer, signal or conditional start event and no such method ends
 * the startup of the application, with a message naming the process and showing the
 * method to write. The check runs while the models are deployed rather than when the
 * start fires, because a timer at three in the morning is a bad moment to find out.
 * <p>
 * The method may take a {@link BpmsStartTrigger} and {@link TaskParam} annotated
 * process variables in any order, which is the same binding a {@link WorkflowTask}
 * method has. It runs in the transaction VanillaBP opened for the start, and what it
 * returns is saved. Throwing means the workflow does not start: nothing is written and
 * the BPMS applies its retry semantics.
 * <p>
 * The ID is the application's choice. A timer brings its trigger time in the
 * {@link BpmsStartTrigger}, and taking that as the ID is what makes a repeated
 * notification harmless: the aggregate is found rather than built a second time. A
 * signal and a condition bring no such value, so an application which wants the same
 * protection there needs a source of its own; an ID the persistence layer generates is
 * fine as long as a second workflow is acceptable.
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
   * The default of {@link #id()}: the method is called for every start the BPMS triggers itself.
   */
  static String ANY_START_EVENT = "";

  /**
   * Which start event this method is interested in. Name one where a process has several of
   * them and an aggregate built for a timer differs from one built for a signal.
   *
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
   * <p>
   * Naming no version does not mean &quot;every version&quot; unconditionally: the method
   * then serves the range of the {@link BpmnProcess} its process was declared with, which
   * is how a whole workflow service class is bound to one generation of a model.
   *
   * @return The versions of the deployed BPMN process this method serves.
   */
  String[] version() default "*";

}
