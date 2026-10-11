package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.vanillabp.spi.process.ProcessService;

/**
 * Marks the parameter which receives the id of the task. With this id the task is
 * completed later, asynchronously. If the BPMS cannot complete this task
 * asynchronously, the value of the parameter is null.
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "myFormKey")
 * public void doForm(
 *         final MyWorkflowAggregate aggregate,
 *         final &#64;TaskId String id) throws {@link TaskException} {
 *     ...
 * </pre>
 * 
 * <i>Hint:</i> If a parameter has this annotation, the task is not completed
 * automatically when the workflow-task method returns. You complete it by calling
 * {@link ProcessService#completeUserTask(Object, String)} or
 * {@link ProcessService#cancelUserTask(Object, String, String)} for user-tasks
 * or {@link ProcessService#completeTask(Object, String)} or
 * {@link ProcessService#cancelTask(Object, String, String)} for workflow-tasks.
 * <p>
 * Decision 7 in the repository's DECISIONS.md says why this one parameter makes a task
 * asynchronous. It also says why a contradiction with the model is reported while the application
 * starts.
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskId {

}
