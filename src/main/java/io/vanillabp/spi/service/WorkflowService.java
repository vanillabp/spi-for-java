package io.vanillabp.spi.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to wire workflow-services to the processes they are responsible for.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface WorkflowService {

    /**
     * @return The workflow-aggregate associated with the annotated service
     */
    Class<?> workflowAggregateClass();

    /**
     * @return The process definition id as defined in the BPMN for which the
     *         annotated service is responsible for. Defaults to the bean name of
     *         the service. This id is used to start new workflows and
     *         correlate messages not related to a dedicated workflow.
     */
    BpmnProcess bpmnProcess() default @BpmnProcess(bpmnProcessId = BpmnProcess.USE_CLASS_NAME);

    /**
     * @return Any additional process definition ids as defined in the BPMN for which the
     *         annotated service is responsible for.
     */
    BpmnProcess[] secondaryBpmnProcesses() default {};
}
