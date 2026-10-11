package io.vanillabp.spi.service;

import java.util.Collection;
import java.util.Map;

/**
 * Builds one value out of every multi-instance iteration a task runs in.
 * <p>
 * This is for a task inside several multi-instance elements. The method does not need three
 * parameters per level. It takes the value built here, and the next task at the same place
 * takes the same value. An implementation is a bean of the application, named by
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
     * The item of this iteration. It is null if the model names no item. That is the case for an
     * iteration driven by a cardinality, which only says how often to repeat.
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
   * The multi-instance elements this resolver wants to get. VanillaBP reads them while the
   * application starts. So if a model has no item at one of them, this is reported at startup,
   * not at the first workflow.
   *
   * @return The BPMN ids of the multi-instance elements
   */
  Collection<String> getNames();

  /**
   * Builds the value for a parameter of a {@link WorkflowTask} method. The parameter
   * is annotated with {@link MultiInstanceElement}, and its
   * {@link MultiInstanceElement#resolverBean()} is set.
   * 
   * @param workflowAggregate The current workflow's aggregate
   * @param multiInstances A sorted map with the context of every active
   *                       multi-instance execution. The key is the name of the
   *                       multi-instance element. The order goes from the
   *                       outermost to the innermost execution.
   * @return A value which will be passed as a parameter
   */
  T resolve(
      A workflowAggregate,
      Map<String, MultiInstance<Object>> multiInstances);

}
