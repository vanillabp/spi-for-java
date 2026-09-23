package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Hands the number of iterations of a multi-instance element to a parameter of a
 * {@link WorkflowTask} method. The parameter is an {@code int} or an {@link Integer}, and it is
 * -1 where the BPMS does not report a number.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceTotal {

  /**
   * The BPMN id of the multi-instance element whose iterations are counted. It is the same id
   * {@link MultiInstanceElement} names, because a task may sit inside several iterations at once.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value();

}
