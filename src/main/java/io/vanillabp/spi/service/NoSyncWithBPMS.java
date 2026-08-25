package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a workflow aggregate, one of its attributes or a getter as <b>NOT shared
 * with the BPMS</b> - the counterpart of {@link SyncWithBPMS}. Use it to keep
 * secrets, large blobs or anything the BPMN model has no business knowing out of
 * the BPMS:
 *
 * <pre>
 * public class Order {
 *
 *   &#64;NoSyncWithBPMS
 *   private String creditCardNumber;
 *
 * }
 * </pre>
 *
 * <b>Inheritance:</b> like {@link SyncWithBPMS} the annotation applies to the
 * annotated element AND everything below it (a nested object's attributes, a
 * collection's elements) until an inner element says otherwise.
 * <p>
 * Why what a BPMS gets to see is declared rather than derived is decision 3 in the repository's
 * DECISIONS.md.
 *
 * @see SyncWithBPMS
 */
@Retention(RUNTIME)
@Target({
    ElementType.TYPE, ElementType.FIELD, ElementType.METHOD
})
@Inherited
@Documented
public @interface NoSyncWithBPMS {

}
