package org.crp.flowable.ai.automation.impl.cmd;

import org.crp.flowable.ai.automation.impl.DynamicServiceTaskBuilderImpl;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.util.List;

import static org.crp.flowable.ai.automation.impl.cmd.InjectionHelper.getActivityBehaviorFactory;

public class InjectServiceTaskInExecutionCmd extends AbstractDynamicInjectionCmd implements Command<Void> {
    private final DynamicServiceTaskBuilderImpl dynamicServiceTaskBuilder;
    private final InjectionHelper injectionHelper;

    public InjectServiceTaskInExecutionCmd(String executionId, DynamicServiceTaskBuilderImpl dynamicServiceTaskBuilder) {
        this.injectionHelper = new InjectionHelper(executionId);
        this.dynamicServiceTaskBuilder = dynamicServiceTaskBuilder;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext, injectionHelper.getExecutionEntity().getProcessInstanceId());
        return null;
    }

    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process, BpmnModel bpmnModel, ProcessDefinitionEntity originalProcessDefinitionEntity, DeploymentEntity newDeploymentEntity) {
        FlowElement flowElement = process.getFlowElement(injectionHelper.getExecutionEntity().getActivityId());
        if (flowElement instanceof FlowNode currentFlowNode) {
            InjectionHelper.removeOutgoingFlows(process, currentFlowNode);
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setId(dynamicServiceTaskBuilder.getId());
            serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
            serviceTask.setImplementation(dynamicServiceTaskBuilder.getExpression());
            serviceTask.setResultVariableName(dynamicServiceTaskBuilder.getResultVariableName());
            SequenceFlow sequenceFlow = new SequenceFlow(currentFlowNode.getId(), serviceTask.getId());
            serviceTask.setIncomingFlows(List.of(sequenceFlow));
            serviceTask.setBehavior(getBehavior(commandContext, serviceTask));
            sequenceFlow.setTargetFlowElement(serviceTask);
            sequenceFlow.setSourceFlowElement(currentFlowNode);
            process.addFlowElement(sequenceFlow);
            process.addFlowElement(serviceTask);
            currentFlowNode.setOutgoingFlows(List.of(sequenceFlow));
            injectionHelper.getExecutionEntity().setCurrentFlowElement(flowElement);
        }
    }

    private static ServiceTaskExpressionActivityBehavior getBehavior(CommandContext commandContext, ServiceTask serviceTask) {
        return getActivityBehaviorFactory(commandContext)
                .createServiceTaskExpressionActivityBehavior(serviceTask);
    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, List<ExecutionEntity> childExecutions) {

    }

}
