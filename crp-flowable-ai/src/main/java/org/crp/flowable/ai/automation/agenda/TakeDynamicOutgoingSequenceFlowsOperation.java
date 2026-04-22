package org.crp.flowable.ai.automation.agenda;

import org.flowable.bpmn.model.FlowNode;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.agenda.TakeOutgoingSequenceFlowsOperation;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;

public class TakeDynamicOutgoingSequenceFlowsOperation extends TakeOutgoingSequenceFlowsOperation {

    public TakeDynamicOutgoingSequenceFlowsOperation(CommandContext commandContext, ExecutionEntity executionEntity, boolean evaluateConditions, boolean forcedSynchronous) {
        super(commandContext, executionEntity, evaluateConditions, forcedSynchronous);
    }

    @Override
    protected void leaveFlowNode(FlowNode flowNode) {
        // Use updated flow node.
        super.leaveFlowNode(getFlowNode(getCurrentFlowElement(execution)));
    }

}
