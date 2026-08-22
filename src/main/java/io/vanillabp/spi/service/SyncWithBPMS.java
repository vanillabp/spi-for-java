package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a workflow aggregate, one of its attributes or an intention-revealing
 * getter as <b>shared with the BPMS</b>: its value is available to the BPMN model
 * (e.g. for gateway conditions).
 *
 * <pre>
 * &#64;NoSyncWithBPMS               // nothing is shared by default ...
 * public class Order {
 *
 *   private ItemSize size;
 *
 *   &#64;SyncWithBPMS                // ... except what the BPMN really needs
 *   public boolean isShippedAsBigItem() { return size == ItemSize.BIG; }
 *
 * }
 * </pre>
 *
 * <b>Inheritance:</b> an attribute which is annotated neither way inherits the
 * behavior of its owner - the aggregate class, respectively the attribute it
 * belongs to (nested objects and collection elements included). The outermost
 * default belongs to the BPMS adapter, and every VanillaBP adapter shares
 * everything: an aggregate carrying no annotation at all reaches its model
 * completely, on an embedded engine as well as on a remote one. A model may
 * therefore read the same attributes wherever it runs, which is the reason the
 * default is not the adapter's taste.
 * <p>
 * <b>What sharing means</b> is the same everywhere, too: the values are written as
 * process variables whenever VanillaBP talks to the BPMS on behalf of the workflow,
 * and the BPMS evaluates its models against those variables. An embedded engine
 * could reach into the application instead, and Camunda 7 did until it turned out
 * that a model written that way breaks on every remote BPMS.
 *
 * @see NoSyncWithBPMS
 */
@Retention(RUNTIME)
@Target({
    ElementType.TYPE, ElementType.FIELD, ElementType.METHOD
})
@Inherited
@Documented
public @interface SyncWithBPMS {

}
