package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.vanillabp.spi.process.ProcessService;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a parameter which will be used to hand-over
 * the task's id. This id is used to complete the task asynchronously. If this
 * task cannot be completed asynchronously due to the BPMS' underlying implementation
 * the value of the parameter will be null.
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "myFormKey")
 * public void doForm(
 *         final MyWorkflowAggregate aggregate,
 *         final &#64;TaskId String id) throws {@link TaskException} {
 *     ...
 * </pre>
 * 
 * <i>Hint:</i> If this annotation is found on a parameter then the task is not
 * completed automatically after processing the workflow-task method. It has to
 * be completed by calling
 * {@link ProcessService#completeUserTask(Object, String)} or
 * {@link ProcessService#cancelUserTask(Object, String, String)} for user-tasks
 * or {@link ProcessService#completeTask(Object, String)} or
 * {@link ProcessService#cancelTask(Object, String, String)} for workflow-tasks.
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskId {

}
