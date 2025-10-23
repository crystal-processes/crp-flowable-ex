package org.crp.flowable.ai.automation.impl;

import org.crp.flowable.ai.automation.AiDynamicBpmnService;
import org.crp.flowable.ai.automation.AiDynamicUserTaskBuilder;
import org.crp.flowable.ai.automation.DynamicServiceTaskBuilder;
import org.crp.flowable.ai.automation.impl.cmd.InjectServiceTaskInExecutionCmd;
import org.crp.flowable.ai.automation.impl.cmd.InjectUserTaskInExecutionCmd;
import org.flowable.common.engine.impl.service.CommonEngineServiceImpl;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class AiDynamicBpmnServiceImpl extends CommonEngineServiceImpl<ProcessEngineConfigurationImpl> implements AiDynamicBpmnService {

    public AiDynamicBpmnServiceImpl(ProcessEngineConfigurationImpl configuration) {
        super(configuration);
    }


    protected void injectServiceTaskInExecution(String executionId, DynamicServiceTaskBuilderImpl dynamicServiceTaskBuilder) {
        configuration.getCommandExecutor().execute(new InjectServiceTaskInExecutionCmd(executionId, dynamicServiceTaskBuilder));
    }

    protected void injectUserTaskInExecution(String executionId, DynamicUserTaskBuilderImpl dynamicUserTaskBuilder) {
        configuration.getCommandExecutor().execute(new InjectUserTaskInExecutionCmd(executionId, dynamicUserTaskBuilder));
    }

    @Override
    public DynamicServiceTaskBuilder createDynamicServiceTaskBuilder() {
        return new DynamicServiceTaskBuilderImpl(this);
    }

    @Override
    public AiDynamicUserTaskBuilder createDynamicUserTaskBuilder() {
        return new DynamicUserTaskBuilderImpl(this);
    }
}
