package org.crp.flowable.ai.automation.impl;


import org.crp.flowable.ai.automation.AiDynamicUserTaskBuilder;

public class DynamicUserTaskBuilderImpl implements AiDynamicUserTaskBuilder {

    protected String id;
    protected String name;
    protected String assignee;
    protected final  AiDynamicBpmnServiceImpl aiDynamicBpmnService;

    public DynamicUserTaskBuilderImpl(AiDynamicBpmnServiceImpl aiDynamicBpmnService) {
        this.aiDynamicBpmnService = aiDynamicBpmnService;
    }

    public String getId() {
        return id;
    }

    public AiDynamicUserTaskBuilder id(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AiDynamicUserTaskBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public AiDynamicUserTaskBuilder assignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    @Override
    public void injectIntoExecution(String executionId) {
        aiDynamicBpmnService.injectUserTaskInExecution(executionId, this);
    }

    public String getAssignee() {
        return assignee;
    }
}
