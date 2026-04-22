package org.crp.flowable.ai.automation.impl;

import org.crp.flowable.ai.automation.DynamicServiceTaskBuilder;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;

public class DynamicServiceTaskBuilderImpl implements org.crp.flowable.ai.automation.DynamicServiceTaskBuilder {
    protected String id;
    protected String expression;
    protected String resultVariableName;
    protected final AiDynamicBpmnServiceImpl dynamicBpmnService;

    public DynamicServiceTaskBuilderImpl(AiDynamicBpmnServiceImpl dynamicBpmnService) {
        this.dynamicBpmnService = dynamicBpmnService;
    }

    public DynamicServiceTaskBuilder id(String id) {
        this.id = id;
        return this;
    }

    public DynamicServiceTaskBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }

    public DynamicServiceTaskBuilder resultVariableName(String resultVariableName) {
        this.resultVariableName = resultVariableName;
        return this;
    }


    public String getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public String getResultVariableName() {
        return resultVariableName;
    }

    public void injectIntoExecution(String executionId) {
        dynamicBpmnService.injectServiceTaskInExecution(executionId, this);
    }

}
