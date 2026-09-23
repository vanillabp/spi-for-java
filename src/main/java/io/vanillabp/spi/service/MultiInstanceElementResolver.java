package io.vanillabp.spi.service;

import java.util.Collection;
import java.util.Map;

/**
 * Builds one value out of every multi-instance iteration a task runs in.
 * <p>
 * A task nested in several multi-instance elements is what this exists for. Instead of three
 * parameters per level, the method takes the value built here, and the next task in the same
 * nesting takes the same one. An implementation is a bean of the application, named by
 * {@link MultiInstanceElement#resolverBean()}.
 *
 * @param <A> The workflow aggregate the resolver may read
 * @param <T> The value handed to the task method
 */
public interface MultiInstanceElementResolver<A, T> {

  /**
   * What one multi-instance element does in the iteration the task runs in.
   *
   * @param <E> The item the iteration works on
   */
  interface MultiInstance<E> {
    /**
     * The item of this iteration. It is null where the model names no item, which is what an
     * iteration driven by a cardinality looks like.
     *
     * @return The item this iteration works on
     */
    E getElement();

    /**
     * Which iteration this is, counted from zero.
     *
     * @return The index of this iteration
     */
    int getIndex();

    /**
     * How many iterations there are, or -1 where the BPMS does not report a number.
     *
     * @return The number of iterations
     */
    int getTotal();
  }

  /**
   * The multi-instance elements this resolver wants to be handed. VanillaBP reads them while the
   * application starts, so a model which hands out no item at one of them is reported then rather
   * than at the first workflow.
   *
   * @return The BPMN ids of the multi-instance elements
   */
  Collection<String> getNames();

  /**
   * Determines an object passed as a {@link WorkflowTask} annotated method's
   * parameter annotated by {@link MultiInstanceElement} having an attribute
   * {@link MultiInstanceElement#resolverBean()} set.
   * 
   * @param workflowAggregate The current workflow's aggregate
   * @param multiInstances a sorted map of all context-information for all active
   *                       multi-instance executions. Key is the name of the
   *                       multi-instance element. The order is from most-out to
   *                       most-inner execution.
   * @return A value which will be passed as a parameter
   */
  T resolve(
      A workflowAggregate,
      Map<String, MultiInstance<Object>> multiInstances);

}
