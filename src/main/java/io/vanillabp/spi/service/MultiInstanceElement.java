package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Passes the item of a multi-instance iteration to a parameter of a {@link WorkflowTask} method.
 * <p>
 * A task may be inside more than one iteration at a time. So {@link #value()} names the BPMN id of
 * the element it asks about. If that gets too much, name a {@link #resolverBean()} instead, and let
 * it build one value out of all the levels.
 * <p>
 * Set exactly one of the two. If you set both or neither, the application does not start, and the
 * message points at the parameter.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceElement {

  /**
   * The default of {@link #value()}: no element is named here, so a {@link #resolverBean()} has
   * to be named.
   */
  String USE_RESOLVER = "";

  /**
   * The BPMN id of the multi-instance element whose item you want. If the BPMS did not report this
   * id for the task, the task ends with a message which lists the ids it did report.
   *
   * @return The BPMN id of the multi-instance element
   */
  String value() default USE_RESOLVER;

  /**
   * A bean which builds the value out of every iteration the task runs in. VanillaBP looks it up
   * when the task runs, not while the application starts.
   *
   * @return The resolver to ask, {@link NoResolver} where an element is named instead
   */
  Class<? extends MultiInstanceElementResolver<?, ?>> resolverBean() default NoResolver.class;

}
