package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a parameter of a {@link WorkflowTask} method. The parameter receives a value
 * of the task (e.g. of a service task or a send task):
 *
 * <pre>
 * &#64;WorkflowTask
 * public void setStatus(
 *         final MyWorkflowAggregate aggregate,
 *         &#64;TaskParam("status") String status) throws {@link TaskException} {
 * </pre>
 *
 * The status has to be defined as an input mapping of the task.
 * <p>
 * The parameter may be declared as a String, as any of the number types, as a
 * Boolean or as Object. VanillaBP converts the value the BPMS reported into that
 * type. A number is converted only if the conversion keeps the number as it is. If
 * the value would arrive as a different number, the task ends instead, with a
 * message which names the value and the declared type. Declare the parameter as
 * Object to see the value the way the BPMS sent it. The README of the migration
 * adapter, in the platform, lists which pairs convert and which are refused.
 * <p>
 * Some types your BPMS may hand back as something else. Some declarations name no
 * type at all, such as {@code Object}. Both are reported while the application
 * starts. You then say once that you know what your BPMS makes of that value
 * (<code>declared-task-params</code>), and the message shows the key to write.
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface TaskParam {

  /**
   * The name which the input mapping of the BPMN element gives the value. That name belongs to the
   * element. So it is not an attribute of the workflow aggregate, and another task cannot see it.
   *
   * @return The name of the local variables mapped in BPMN.
   */
  String value();

}
