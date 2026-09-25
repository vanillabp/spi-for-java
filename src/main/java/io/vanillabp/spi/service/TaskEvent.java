package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a parameter for processing a particular
 * task event.
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "myFormKey")
 * public void setStatus(
 *         final MyWorkflowAggregate aggregate,
 *         &#64;TaskEvent("ALL") {@link Event} event,
 *         &#64;UserTaskId String id) throws {@link TaskException} {
 * </pre>
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskEvent {

  /**
   * The two moments of a task at which a method can be called. A parameter is handed the moment
   * which actually happened, so it never sees {@link #ALL}.
   * <p>
   * Where {@link #CANCELED} comes from depends on the BPMS. An engine which fires an event per
   * element delivers it like any other notification, and a BPMS which can only say that a whole
   * workflow ended has the cancellation worked out for it: VanillaBP reads the tasks it still
   * believes are open in that workflow and reports each of them. Either way the application sees
   * the same moment.
   */
  enum Event {
    /**
     * filter to events on creating a task
     */
    CREATED,
    /**
     * filter to events on canceling a user task or an asynchronous task (e.g. due to boundary event)
     */
    CANCELED,
    /**
     * no filtering
     */
    ALL,
  };

  /**
   * The moments the annotated method is called at. Several {@link TaskEvent} parameters of one
   * method add up instead of narrowing each other, and a moment nobody asked for is dropped
   * before the workflow aggregate is even loaded.
   *
   * @return The moments to be called at
   */
  public Event[] value() default {
      Event.ALL
  };

}
