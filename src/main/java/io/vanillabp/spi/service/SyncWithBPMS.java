package io.vanillabp.spi.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a workflow aggregate, one of its attributes or a getter whose name says
 * what it means as <b>shared with the BPMS</b>. The BPMN model can then use its
 * value (e.g. in gateway conditions).
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
 * <b>Inheritance:</b> an attribute without either annotation behaves like its
 * owner. The owner is the aggregate class, or the attribute it belongs to (nested
 * objects and collection elements included). The outermost default belongs to the
 * BPMS adapter, and every VanillaBP adapter shares everything. So an aggregate
 * without any annotation reaches its model completely, on an embedded engine as
 * well as on a remote one. A model can read the same attributes wherever it runs.
 * That is why the default is not left to each adapter.
 * <p>
 * <b>What sharing means</b> is the same everywhere, too. VanillaBP writes the values
 * as process variables whenever it talks to the BPMS for the workflow, and the BPMS
 * evaluates its models against those variables. An embedded engine could reach into
 * the application instead. Camunda 7 did that, until it turned out that a model
 * written that way breaks on every remote BPMS.
 * <p>
 * <b>An aggregate which shares everything has to say so.</b> An application which annotates
 * nothing shares everything, so the platform integration does not start such a workflow
 * silently. Either the aggregate says what its models need, which we recommend. Or the workflow
 * allows the full sync in the configuration
 * (<code>vanillabp.workflow-modules.&lt;module&gt;.workflows.&lt;process&gt;.allow-full-sync-with-bpms</code>).
 * <p>
 * See decision 3 in the repository's DECISIONS.md. It says why you declare what a BPMS gets to
 * see, instead of VanillaBP working it out. It also says what this has to do with running the
 * same model on another BPMS.
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
