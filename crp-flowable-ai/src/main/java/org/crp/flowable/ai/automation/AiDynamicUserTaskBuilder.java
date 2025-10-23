package org.crp.flowable.ai.automation;

public interface AiDynamicUserTaskBuilder {

    AiDynamicUserTaskBuilder id(String id);

    AiDynamicUserTaskBuilder name(String name);

    AiDynamicUserTaskBuilder assignee(String assignee);

    void injectIntoExecution(String executionId);
}
