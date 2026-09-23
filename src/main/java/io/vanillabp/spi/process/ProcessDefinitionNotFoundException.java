package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#getBpmnXml(String)} if the given process definition id cannot be
 * resolved. Either it was never reported by
 * {@link ProcessService#getProcessDefinitions(Object, String)}, or it names an adapter which is no
 * longer configured, or the BPMS no longer holds that definition.
 * <p>
 * The ids are opaque and each one names the adapter which can resolve it, because
 * {@code getBpmnXml} has no aggregate to elect a BPMS by. So hand back the id as it was reported
 * and do not build one of your own.
 */
public class ProcessDefinitionNotFoundException extends RuntimeException {

  /**
   * Reports an id which no configured adapter could turn into a BPMN model.
   *
   * @param message What the application is told. Name the id and the reason it did not resolve,
   *                because nothing else of the failed lookup reaches the caller
   */
  public ProcessDefinitionNotFoundException(
      final String message) {
    super(message);
  }

  /**
   * The same report for an adapter which has a failure of its BPMS to hand on. VanillaBP itself
   * uses the shorter constructor: it knows why the id did not resolve and says so in the message.
   *
   * @param message What the application is told
   * @param cause The failure which made the lookup fail
   */
  public ProcessDefinitionNotFoundException(
      final String message,
      final Throwable cause) {
    super(message, cause);
  }
}
