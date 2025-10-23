package org.crp.flowable.ai.automation;

public interface AiDynamicBpmnService {

    DynamicServiceTaskBuilder createDynamicServiceTaskBuilder();

    AiDynamicUserTaskBuilder createDynamicUserTaskBuilder();
}
