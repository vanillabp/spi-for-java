package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#getBpmnXml(String)} if no adapter can find the given process
 * definition id. There are three reasons. The id was never reported by
 * {@link ProcessService#getProcessDefinitions(Object, String)}. Or it names an adapter which is no
 * longer configured. Or the BPMS no longer holds that definition.
 * <p>
 * The ids are opaque: do not read anything into them. Each id names the adapter which can find
 * it, because {@code getBpmnXml} gets no aggregate to choose a BPMS by. So pass the id back
 * exactly as it was reported, and do not build one of your own.
 */
public class ProcessDefinitionNotFoundException extends RuntimeException {

  /**
   * Reports an id which no configured adapter could turn into a BPMN model.
   *
   * @param message What the application is told. Name the id and the reason why it was not found,
   *                because the caller learns nothing else about the failed lookup
   */
  public ProcessDefinitionNotFoundException(
      final String message) {
    super(message);
  }

  /**
   * The same report, for an adapter which has to pass on a failure of its BPMS. VanillaBP itself
   * uses the shorter constructor. It knows why the id was not found and says so in the message.
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
