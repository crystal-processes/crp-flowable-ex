package org.crp.flowable.ai.automation.impl.cmd;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;

import java.io.Serializable;

public class StartAiProcessInstanceCmd implements Command<ProcessInstance>, Serializable {

    protected ProcessInstanceBuilder processInstanceBuilder;

    public StartAiProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder) {
        this.processInstanceBuilder = processInstanceBuilder;
    }

    @Override
    public ProcessInstance execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        ProcessDefinition processDefinition = getProcessDefinition(processEngineConfiguration, commandContext);

        return startProcessInstance();

    }

    private ProcessInstance startProcessInstance() {
        return null;
    }

    private ProcessDefinition getProcessDefinition(ProcessEngineConfigurationImpl processEngineConfiguration, CommandContext commandContext) {
        return null;
    }
}
