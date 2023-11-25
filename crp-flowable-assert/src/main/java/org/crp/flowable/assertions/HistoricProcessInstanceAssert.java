package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;

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
        processExistsInHistory();

        return assertThat(getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(actual.getId())
                .orderByHistoricActivityInstanceStartTime().desc().list());
    }

    /**
     * Assert <b>historic</b> process instance exists in the history and is finished.
     *
     * @return Historic process instance assertion.
     */
    public HistoricProcessInstanceAssert isFinished() {
        processExistsInHistory();

        if (getHistoryService().createHistoricProcessInstanceQuery().finished().processInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getProcessDescription(actual)+" to be finished, but is running in history.");
        }

        return this;
    }

    public ListAssert<HistoricVariableInstance> variables() {
        processExistsInHistory();

        return assertThat(getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(actual.getId()).orderByVariableName().asc().list());
    }

    /**
     * Assert that process instance has variable in <b>history</b>.
     *
     * @param variableName variable to check.
     * @return Historic process instance assertion
     */

    public HistoricProcessInstanceAssert hasVariable(String variableName) {
        processExistsInHistory();

        if (getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(actual.getId()).variableExists(variableName).count() != 1) {
            failWithMessage(getProcessDescription(actual)+" has variable <%s> but variable does not exist in history.", variableName);
        }

        return this;
    }

    /**
     * Assert that process instance does not have variable in <b>history</b>.
     * @param variableName variable to check
     * @return Historic process instance assertion
     */
    public HistoricProcessInstanceAssert doesNotHaveVariable(String variableName) {
        processExistsInHistory();

        if (getRuntimeService().createProcessInstanceQuery().processInstanceId(actual.getId()).variableExists(variableName).count() != 0) {
            failWithMessage(getProcessDescription(actual)+" does not have variable <%s> but variable exists in history.", variableName);
        }

        return this;
    }

    /**
     * Assert that process instance has variable in <b>history</b> with value equals to expectedValue.
     *
     * @param variableName variable to check.
     * @param expectedValue expected variable value.
     * @return Historic process instance assertion
     */
    public HistoricProcessInstanceAssert hasVariableWithValue(String variableName, Object expectedValue) {
        processExistsInHistory();
        hasVariable(variableName);

        VariableInstance actualVariable = getRuntimeService().createVariableInstanceQuery().processInstanceId(actual.getId()).variableName(variableName).singleResult();
        Assertions.assertThat(actualVariable.getValue()).isEqualTo(expectedValue);
        return this;
    }

    /**
     * Assert list of <b>runtime</b> identity links without ordering.
     *
     * @return Assertion of #{@link IdentityLink} list.
     */
    public ListAssert<HistoricIdentityLink> identityLinks() {
        processExistsInHistory();

        return assertThat(getHistoryService().getHistoricIdentityLinksForProcessInstance(actual.getId()));
    }

    /**
     * Assert list of user tasks in the <b>history</b> ordered by the task name ascending.
     * Process, Task variables and identityLinks are included.
     *
     * @return Assertion of {@link HistoricTaskInstance} list.
     */
    public ListAssert<HistoricTaskInstance> userTasks() {
        processExistsInHistory();

        return assertThat(getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(actual.getId()).orderByTaskName().asc()
                .includeProcessVariables().includeIdentityLinks().includeTaskLocalVariables().list());
    }

    private void processExistsInHistory() {
        isNotNull();
        isInHistory();
    }

    private void isInHistory() {
        if (getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getProcessDescription(actual)+"> exists in history but process instance not found.");
        }
    }

}
