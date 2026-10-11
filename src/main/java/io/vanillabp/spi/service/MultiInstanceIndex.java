package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Passes the number of a multi-instance iteration to a parameter of a {@link WorkflowTask} method,
 * counted from zero. The parameter is an {@code int} or an {@link Integer}.
 * <p>
 * If a task iterates over ids which its own aggregate holds, it needs nothing else. The index says
 * which id is meant, and the task fetches it itself. The BPMS does not have to carry it.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceIndex {

  /**
   * The BPMN id of the multi-instance element whose iteration is meant. It is the same id that
   * {@link MultiInstanceElement} names, because a task may be inside several iterations at once.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value();

}
