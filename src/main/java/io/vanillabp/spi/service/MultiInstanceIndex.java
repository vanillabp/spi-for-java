package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Hands the number of a multi-instance iteration to a parameter of a {@link WorkflowTask} method,
 * counted from zero. The parameter is an {@code int} or an {@link Integer}.
 * <p>
 * A task iterating over ids its own aggregate holds needs nothing else: the index says which of
 * them is meant, and the task fetches it itself instead of letting the BPMS carry it.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceIndex {

  /**
   * The BPMN id of the multi-instance element whose iteration is meant. It is the same id
   * {@link MultiInstanceElement} names, because a task may sit inside several iterations at once.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value();

}
