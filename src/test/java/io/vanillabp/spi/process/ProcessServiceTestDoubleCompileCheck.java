package io.vanillabp.spi.process;

/**
 * Compile-time guard: a hand-written test double of {@link ProcessService}
 * implementing only the pre-1.1.0 surface plus {@link ProcessService#getWorkflowModuleId()}
 * must keep compiling. Query methods added later (viewer/history API,
 * {@code startWorkflowByMessage}) are {@code default} methods on purpose — adding
 * new abstract methods to {@link ProcessService} breaks user test code and fails
 * this class's compilation.
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
