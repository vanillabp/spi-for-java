package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a method for processing a certain
 * process-task (e.g. service-task, send-task, etc.):
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "doSomeWorkload")
 * public void doSomeWorkload(final MyWorkflowAggregate aggregate) throws {@link TaskException} {
 * </pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
@Repeatable(WorkflowTasks.class)
public @interface WorkflowTask {

    static String USE_METHOD_NAME = "";

    /**
     * @return The activity's BPMN id. Defaults to the annotated method's name.
     */
    String id() default USE_METHOD_NAME;

    /**
     * @return The task-definition as defined in the BPMN. Defaults to the annotated
     *         method's name.
     */
    String taskDefinition() default USE_METHOD_NAME;

    /**
     * Can be used to define certain versions or ranges of versions of a process for
     * which the annotated method should be used for.
     * <p>
     * Format:
     * <ul>
     * <li><i>*</i>: all versions
     * <li><i>1</i>: only version &quot;1&quot;
     * <li><i>1-3</i>: only versions &quot;1&quot;, &quot;2&quot; and &quot;3&quot;
     * <li><i>&gt;3</i>: only versions less than &quot;3&quot;
     * <li><i>&lt;3</i>: only versions higher than &quot;3&quot;</li>
     * </ul>
     * 
     * @throws RuntimeException If a process' version does not match any method
     *                          annotated.
     * @return The version of the process this method belongs to.
     */
    String[] version() default "*";

}
