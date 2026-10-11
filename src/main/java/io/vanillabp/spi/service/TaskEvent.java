package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the parameter which receives the event of a task, for example that it was
 * created or canceled.
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
   * The two moments of a task at which a method can be called. A parameter gets the moment which
   * really happened, so it never sees {@link #ALL}.
   * <p>
   * Where {@link #CANCELED} comes from depends on the BPMS. An engine which fires an event per
   * element delivers it like any other notification. Some BPMS can only say that a whole workflow
   * ended. For them VanillaBP works the cancellation out: it reads the tasks of that workflow which
   * it still thinks are open, and reports each of them. Either way the application sees the same
   * moment.
   */
  enum Event {
    /**
     * Only the event of a task being created
     */
    CREATED,
    /**
     * Only the event of a user task or an asynchronous task being canceled (e.g. by a boundary event)
     */
    CANCELED,
    /**
     * Every event, nothing is filtered
     */
    ALL,
  };

  /**
   * The moments at which the annotated method is called. If one method has several
   * {@link TaskEvent} parameters, their moments add up. They do not narrow each other. A moment
   * nobody asked for is dropped before the workflow aggregate is even loaded.
   *
   * @return The moments to be called at
   */
  public Event[] value() default {
      Event.ALL
  };

}
