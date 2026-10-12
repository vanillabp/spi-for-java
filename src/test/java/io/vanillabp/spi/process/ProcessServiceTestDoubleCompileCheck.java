package io.vanillabp.spi.process;

/**
 * Holds that a test double of {@link ProcessService} written by hand still compiles. The double
 * implements only the methods of the API before 1.1.0, plus
 * {@link ProcessService#getWorkflowModuleId()}. Every method added later, like the queries for
 * history and viewers or {@code startWorkflowByMessage}, is a {@code default} method on purpose.
 * A new abstract method would break the test code of our users, and this class stops compiling.
 */
class ProcessServiceTestDoubleCompileCheck implements ProcessService<Object> {

  @Override
  public Object startWorkflow(
      final Object workflowAggregate) {
    return workflowAggregate;
  }

  @Override
  public Object correlateMessage(
      final Object workflowAggregate,
      final String messageName) {
    return workflowAggregate;
  }

  @Override
  public Object correlateMessage(
      final Object workflowAggregate,
      final String messageName,
      final String correlationId) {
    return workflowAggregate;
  }

  @Override
  public Object completeUserTask(
      final Object workflowAggregate,
      final String taskId) {
    return workflowAggregate;
  }

  @Override
  public Object cancelUserTask(
      final Object workflowAggregate,
      final String taskId,
      final String bpmnErrorCode) {
    return workflowAggregate;
  }

  @Override
  public Object completeTask(
      final Object workflowAggregate,
      final String taskId) {
    return workflowAggregate;
  }

  @Override
  public Object cancelTask(
      final Object workflowAggregate,
      final String taskId,
      final String bpmnErrorCode) {
    return workflowAggregate;
  }

  @Override
  public String getWorkflowModuleId() {
    return "compile-check";
  }

}
