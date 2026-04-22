package org.crp.flowable.ai.automation.impl.cmd;

import org.crp.flowable.ai.automation.impl.DynamicUserTaskBuilderImpl;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.util.List;

import static org.crp.flowable.ai.automation.impl.cmd.InjectionHelper.getActivityBehaviorFactory;
import static org.crp.flowable.ai.automation.impl.cmd.InjectionHelper.removeOutgoingFlows;

public class InjectUserTaskInExecutionCmd extends AbstractDynamicInjectionCmd implements Command<Void> {
    protected final InjectionHelper injectionHelper;
    protected final DynamicUserTaskBuilderImpl dynamicUserTaskBuilder;

    public InjectUserTaskInExecutionCmd(String executionId, DynamicUserTaskBuilderImpl dynamicUserTaskBuilder) {
        this.injectionHelper = new InjectionHelper(executionId);
        this.dynamicUserTaskBuilder = dynamicUserTaskBuilder;
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
            removeOutgoingFlows(process, currentFlowNode);
            UserTask task = new UserTask();
            task.setId(dynamicUserTaskBuilder.getId());
            task.setAssignee(dynamicUserTaskBuilder.getAssignee());
            task.setName(dynamicUserTaskBuilder.getName());
            SequenceFlow sequenceFlow = new SequenceFlow(currentFlowNode.getId(), task.getId());
            task.setIncomingFlows(List.of(sequenceFlow));
            task.setBehavior(getBehavior(commandContext, task));
            sequenceFlow.setTargetFlowElement(task);
            sequenceFlow.setSourceFlowElement(currentFlowNode);
            process.addFlowElement(sequenceFlow);
            process.addFlowElement(task);
            currentFlowNode.setOutgoingFlows(List.of(sequenceFlow));
            injectionHelper.getExecutionEntity().setCurrentFlowElement(flowElement);
        }
    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, List<ExecutionEntity> childExecutions) {

    }

    protected static UserTaskActivityBehavior getBehavior(CommandContext commandContext, UserTask task) {
        return getActivityBehaviorFactory(commandContext)
                .createUserTaskActivityBehavior(task);
    }

}
