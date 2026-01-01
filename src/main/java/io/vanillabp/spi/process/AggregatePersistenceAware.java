package io.vanillabp.spi.process;

/**
 * Implemented by classes which are aware of persisting aggregates of the given type.
 * <p>
 * This interface may be implemented by the platform-specific adapter to provide generic aggregate persistence.
 * However, if the platform does not support this or an aggregate has to be persisted differently, then
 * the business processing application may provide additional implementations (e.g., by the service annotated
 * by the @{@link io.vanillabp.spi.service.WorkflowService} annotation). The implementation with the most specific
 * generic parameter is chosen, also taking superclasses and implemented interfaces into account.
 *
 * @param <A> The aggregate type
 */
public interface AggregatePersistenceAware<A> {

  /**
   * @return The aggregate class.
   */
  Class<A> getAggregateClass();

  /**
   * Persists the given aggregate.
   *
   * @param aggregate The aggregate to persist
   * @return The persisted aggregate, in case of ORM frameworks an attached object is returned.
   */
  A save(
      A aggregate);

  /**
   * @param aggregate The aggregate to investigate
   * @return The aggregate's ID.
   */
  Object getAggregateId(
      A aggregate);

}
