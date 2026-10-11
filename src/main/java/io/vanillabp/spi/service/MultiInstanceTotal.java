package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Passes the number of iterations of a multi-instance element to a parameter of a
 * {@link WorkflowTask} method. The parameter is an {@code int} or an {@link Integer}. It is -1
 * if the BPMS does not report a number.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceTotal {

  /**
   * The BPMN id of the multi-instance element whose iterations are counted. It is the same id that
   * {@link MultiInstanceElement} names, because a task may be inside several iterations at once.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value();

}
