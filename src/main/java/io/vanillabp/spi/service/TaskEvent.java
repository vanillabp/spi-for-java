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

    enum Event {
        CREATED, // filter to events on creating a task
        CANCELED, // filter to events on canceling a user task or an asynchronous task (e.g. due to boundary event)
        ALL, // no filtering, also the 
    };

    public Event[] value() default { Event.ALL };

}
