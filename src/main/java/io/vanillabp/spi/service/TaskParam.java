package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a parameter for processing a certain
 * process-task (e.g. service-task, send-task, etc.):
 * 
 * <pre>
 * &#64;WorkflowTask
 * public void setStatus(
 *         final MyWorkflowAggregate aggregate,
 *         &#64;TaskParam("status") String status) throws {@link TaskException} {
 * </pre>
 * 
 * The status has to be defined an input-mapping of the task.
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskParam {

    /**
     * @return The name of the local variables mapped in BPMN.
     */
    String value();

}
