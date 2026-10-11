package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a workflow aggregate, one of its attributes or a getter as <b>NOT shared
 * with the BPMS</b>. It is the counterpart of {@link SyncWithBPMS}. Use it to keep
 * secrets, large blobs and anything else the BPMN model does not need out of the
 * BPMS:
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
 * <b>Inheritance:</b> like {@link SyncWithBPMS}, the annotation applies to the
 * annotated element AND to everything below it (the attributes of a nested object,
 * the elements of a collection), until an inner element says otherwise.
 * <p>
 * An aggregate which would otherwise share everything needs one of these annotations to be
 * started at all. A workflow whose aggregate keeps nothing back does not start, unless its
 * configuration allows the full sync
 * (<code>vanillabp.workflow-modules.&lt;module&gt;.workflows.&lt;process&gt;.allow-full-sync-with-bpms</code>).
 * <p>
 * Decision 3 in the repository's DECISIONS.md says why you declare what a BPMS gets to see,
 * instead of VanillaBP working it out.
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
