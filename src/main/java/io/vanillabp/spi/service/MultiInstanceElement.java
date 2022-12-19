package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceElement {

    String USE_RESOLVER = "";

    /**
     * @return The name of variable/field which holds the current value of the
     *         multi-instance iteration
     */
    String value() default USE_RESOLVER;

    /**
     * @return The bean-name of the resolver used to determine the current element
     */
    Class<? extends MultiInstanceElementResolver<?, ?>> resolverBean() default NoResolver.class;

}
