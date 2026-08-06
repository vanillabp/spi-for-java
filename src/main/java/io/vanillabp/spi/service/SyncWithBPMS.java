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
 * default is the BPMS ADAPTER's: an embedded engine reading the aggregate live
 * (Camunda 7) shares nothing unless asked to, a remote engine (Camunda 8,
 * Process-Engine-API) shares everything - see your adapter's documentation.
 * <p>
 * <b>What sharing means</b> is the adapter's decision, too: a remote BPMS pushes
 * the values as process variables whenever VanillaBP talks to it on behalf of the
 * workflow, whereas an embedded engine reads the aggregate directly and writes the
 * values only as context information for operators.
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
