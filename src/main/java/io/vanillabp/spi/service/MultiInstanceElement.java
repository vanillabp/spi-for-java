package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Hands the item of a multi-instance iteration to a parameter of a {@link WorkflowTask} method.
 * <p>
 * A task may sit inside more than one iteration at a time, which is why {@link #value()} names the
 * BPMN id of the element it asks about. Where that becomes noise, name a {@link #resolverBean()}
 * instead and let it build one value out of all the levels.
 * <p>
 * Exactly one of the two belongs here. Naming both, or naming neither, stops the application from
 * starting, with a message pointing at the parameter.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceElement {

  /**
   * The default of {@link #value()}: no element is named here, so a {@link #resolverBean()} has
   * to be.
   */
  String USE_RESOLVER = "";

  /**
   * The BPMN id of the multi-instance element whose item is wanted. An id which the BPMS did not
   * report for this task ends the task with a message listing the ids it did report.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value() default USE_RESOLVER;

  /**
   * A bean building the value out of every iteration the task runs in. It is looked up when the
   * task runs, not while the application starts.
   *
   * @return The resolver to ask, {@link NoResolver} where an element is named instead
   */
  Class<? extends MultiInstanceElementResolver<?, ?>> resolverBean() default NoResolver.class;

}
