package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.Utils.*;

public class HistoricProcessInstanceAssert extends AbstractAssert<HistoricProcessInstanceAssert, HistoricProcessInstance> {
    protected HistoricProcessInstanceAssert(HistoricProcessInstance historicProcessInstance) {
        super(historicProcessInstance, HistoricProcessInstanceAssert.class);
    }

    /**
     * Assert <b>historic</b> activities ordered by activity instance start time.
     *
     * @return Assertion of {@link HistoricActivityInstance} list.
     */
    public ListAssert<HistoricActivityInstance> activities() {
        processExistsInHistory(HistoryLevel.ACTIVITY);

        return assertThat(getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(actual.getId())
                .orderByHistoricActivityInstanceStartTime().desc().list());
    }

    public HistoricProcessInstanceAssert isFinished() {
        processExistsInHistory(HistoryLevel.INSTANCE);

        if (getHistoryService().createHistoricProcessInstanceQuery().finished().processInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getProcessDescription(actual)+" to be finished, but is running in history.");
        }

        return this;
    }

    public ListAssert<HistoricVariableInstance> variables() {
        isNotNull();

        return assertThat(getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(actual.getId()).list());
    }

    public ObjectAssert<HistoricVariableInstance> variable(String variableName) {
        isNotNull();

        return assertThat(
                getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(actual.getId()).variableName(variableName).singleResult()
        ).isNotNull();
    }

    public ListAssert<HistoricIdentityLink> identityLinks() {
        isNotNull();

        return assertThat(getHistoryService().getHistoricIdentityLinksForProcessInstance(actual.getId()));
    }

    public ListAssert<HistoricTaskInstance> userTasks() {
        isNotNull();

        return assertThat(getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(actual.getId())
                .includeProcessVariables().includeIdentityLinks().includeTaskLocalVariables().list());
    }

    private void processExistsInHistory(HistoryLevel minHistoryLevel) {
        isNotNull();
        isHistoryLevelAt(minHistoryLevel);
        isInHistory();
    }

    private void isHistoryLevelAt(HistoryLevel historyLevel) {
        if (!Utils.getProcessEngine().getProcessEngineConfiguration().getHistoryLevel().isAtLeast(historyLevel)) {
            failWithMessage("Process engine <%s> does not run at history level <%s>.",
                    Utils.getProcessEngine().getName(), historyLevel);
        }
    }

    private void isInHistory() {
        if (getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getProcessDescription(actual)+"> exists in history but process instance not found.");
        }
    }

}
