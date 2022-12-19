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
public @interface MultiInstanceTotal {

    /**
     * @return The name of variable/field which holds the current total number of
     *         the multi-instance iteration.
     */
    String value();

}
