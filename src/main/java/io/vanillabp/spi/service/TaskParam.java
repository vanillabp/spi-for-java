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
 * <p>
 * The parameter may be declared as a String, as any of the number types, as a
 * Boolean or as Object, and VanillaBP converts the value the BPMS reported into
 * that type. A number is converted only where the conversion keeps it: a value
 * which would arrive as a different number ends the task instead, with a message
 * naming the value and the declared type. Declare the parameter as Object to see
 * the value the way the BPMS sent it. Which pairs convert and which are refused is
 * documented with the platform, in the migration adapter's README.
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskParam {

  /**
   * The name the input mapping of the BPMN element gives the value. That name belongs to the
   * element, so it is neither an attribute of the workflow aggregate nor visible to another task.
   *
   * @return The name of the local variables mapped in BPMN.
   */
  String value();

}
