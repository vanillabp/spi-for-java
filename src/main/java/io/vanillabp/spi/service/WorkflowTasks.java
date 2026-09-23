package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Collects the {@link WorkflowTask} annotations of a method which serves more than one BPMN
 * element, for example the same piece of work modelled at two places of a process.
 * <p>
 * Java needs a container like this for a repeatable annotation. Write {@link WorkflowTask} as
 * often as needed and leave this one to the compiler.
 *
 * @see WorkflowTask
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
public @interface WorkflowTasks {

  /**
   * The annotations the compiler put into this container.
   *
   * @return The {@link WorkflowTask} declarations of the annotated method
   */
  WorkflowTask[] value();

}
