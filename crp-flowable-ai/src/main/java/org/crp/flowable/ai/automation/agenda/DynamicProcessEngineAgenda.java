package org.crp.flowable.ai.automation.agenda;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.impl.agenda.TakeOutgoingSequenceFlowsOperation;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.interceptor.MigrationContext;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class DynamicProcessEngineAgenda implements FlowableEngineAgenda  {
    protected final CommandContext commandContext;
    protected final FlowableEngineAgenda parentAgenda;

    public DynamicProcessEngineAgenda(CommandContext commandContext, FlowableEngineAgenda parentAgenda) {
        this.parentAgenda = parentAgenda;
        this.commandContext = commandContext;
    }

    @Override
    public void planTakeOutgoingSequenceFlowsOperation(ExecutionEntity execution, boolean evaluateConditions) {
        planOperation(new TakeDynamicOutgoingSequenceFlowsOperation(commandContext, execution, evaluateConditions, false), execution);
    }

    @Override
    public void planTakeOutgoingSequenceFlowsSynchronousOperation(ExecutionEntity execution, boolean evaluateConditions) {
        planOperation(new TakeDynamicOutgoingSequenceFlowsOperation(commandContext, execution, evaluateConditions, true), execution);
    }

    // wrqap all other methods to parentAgenda

    @Override
    public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
        parentAgenda.planOperation(operation, executionEntity);
    }

    @Override
    public void planContinueProcessOperation(ExecutionEntity execution) {
        parentAgenda.planContinueProcessOperation(execution);
    }

    @Override
    public void planContinueProcessSynchronousOperation(ExecutionEntity execution) {
        parentAgenda.planContinueProcessSynchronousOperation(execution);
    }

    @Override
    public void planContinueProcessWithMigrationContextOperation(ExecutionEntity execution, MigrationContext migrationContext) {
        parentAgenda.planContinueProcessWithMigrationContextOperation(execution, migrationContext);
    }

    @Override
    public void planContinueProcessInCompensation(ExecutionEntity execution) {
        parentAgenda.planContinueProcessInCompensation(execution);
    }

    @Override
    public void planContinueMultiInstanceOperation(ExecutionEntity execution, ExecutionEntity multiInstanceRootExecution, int loopCounter) {
        parentAgenda.planContinueMultiInstanceOperation(execution, multiInstanceRootExecution, loopCounter);
    }

    @Override
    public void planEndExecutionOperation(ExecutionEntity execution) {
        parentAgenda.planEndExecutionOperation(execution);
    }

    @Override
    public void planEndExecutionOperationSynchronous(ExecutionEntity execution) {
        parentAgenda.planEndExecutionOperationSynchronous(execution);
    }

    @Override
    public void planTriggerExecutionOperation(ExecutionEntity execution) {
        parentAgenda.planTriggerExecutionOperation(execution);
    }

    @Override
    public void planAsyncTriggerExecutionOperation(ExecutionEntity execution) {
        parentAgenda.planAsyncTriggerExecutionOperation(execution);
    }

    @Override
    public void planEvaluateConditionalEventsOperation(ExecutionEntity execution) {
        parentAgenda.planEvaluateConditionalEventsOperation(execution);
    }

    @Override
    public void planEvaluateVariableListenerEventsOperation(String processDefinitionId, String processInstanceId) {
        parentAgenda.planEvaluateVariableListenerEventsOperation(processDefinitionId, processInstanceId);
    }

    @Override
    public void planDestroyScopeOperation(ExecutionEntity execution) {
        parentAgenda.planDestroyScopeOperation(execution);
    }

    @Override
    public void planExecuteInactiveBehaviorsOperation(Collection<ExecutionEntity> executions) {
        parentAgenda.planExecuteInactiveBehaviorsOperation(executions);
    }

    @Override
    public boolean isEmpty() {
        return parentAgenda.isEmpty();
    }

    @Override
    public Runnable getNextOperation() {
        return parentAgenda.getNextOperation();
    }

    @Override
    public void planOperation(Runnable operation) {
        parentAgenda.planOperation(operation);
    }

    @Override
    public <V> void planFutureOperation(CompletableFuture<V> future, BiConsumer<V, Throwable> completeAction) {
        parentAgenda.planFutureOperation(future, completeAction);
    }

    @Override
    public void flush() {
        parentAgenda.flush();
    }

    @Override
    public void close() {
        parentAgenda.close();
    }
}
