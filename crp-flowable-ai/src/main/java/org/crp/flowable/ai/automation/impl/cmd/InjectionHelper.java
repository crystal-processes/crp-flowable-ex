package org.crp.flowable.ai.automation.impl.cmd;

import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.EngineConfigurationConstants;
import org.flowable.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;

public class InjectionHelper {
    private final String executionId;

    public InjectionHelper(String executionId) {
        this.executionId = executionId;
    }

    protected static ActivityBehaviorFactory getActivityBehaviorFactory(CommandContext commandContext) {
        return ((ProcessEngineConfigurationImpl) commandContext.getEngineConfigurations().get(EngineConfigurationConstants.KEY_PROCESS_ENGINE_CONFIG))
                .getActivityBehaviorFactory();
    }

    public static void removeOutgoingFlows(Process process, FlowNode flowNode) {
        flowNode.getOutgoingFlows().forEach(outgoingFlow -> process.removeFlowElement(outgoingFlow.getId()));
    }


    protected ExecutionEntity getExecutionEntity() {
        return getExecutionEntityManager().findById(executionId);
    }


    protected static ExecutionEntityManager getExecutionEntityManager() {
        return CommandContextUtil.getProcessEngineConfiguration().getExecutionEntityManager();
    }

}
