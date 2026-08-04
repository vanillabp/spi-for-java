package io.vanillabp.spi.process;

/**
 * Thrown by {@link ProcessService#completeTask(Object, String)} and
 * {@link ProcessService#cancelTask(Object, String, String)} if no configured BPMS
 * knows the given task: it never existed, its ID is wrong, or it was removed
 * without completion (e.g. the workflow was terminated).
 */
public class TaskNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TaskNotFoundException(
      final String message) {

    super(message);

  }

}
