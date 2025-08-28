package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricMilestoneInstance;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.Utils.getCaseDescription;
import static org.crp.flowable.assertions.Utils.getCmmnHistoryService;

public class HistoricCaseInstanceAssert extends AbstractAssert<HistoricCaseInstanceAssert, HistoricCaseInstance> {
    protected HistoricCaseInstanceAssert(HistoricCaseInstance historicCaseInstance) {
        super(historicCaseInstance, HistoricCaseInstanceAssert.class);
    }

    /**
     * Assert <b>historic</b> activities ordered by activity instance start time.
     *
     * @return Assertion of {@link HistoricActivityInstance} list.
     */
    public ListAssert<HistoricMilestoneInstance> milestones() {
        caseExistsInHistory();

        return assertThat(getCmmnHistoryService().createHistoricMilestoneInstanceQuery().milestoneInstanceCaseInstanceId(actual.getId())
                .orderByTimeStamp().desc().list());
    }

    /**
     * Assert <b>historic</b> case instance exists in the history and is finished.
     *
     * @return Historic case instance assertion.
     */
    public HistoricCaseInstanceAssert isFinished() {
        caseExistsInHistory();

        if (getCmmnHistoryService().createHistoricCaseInstanceQuery().finished().caseInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getCaseDescription(actual)+" to be finished, but is running in history.");
        }

        return this;
    }

    public ListAssert<HistoricVariableInstance> variables() {
        caseExistsInHistory();

        return assertThat(getCmmnHistoryService().createHistoricVariableInstanceQuery().caseInstanceId(actual.getId()).orderByVariableName().asc().list());
    }

    /**
     * Assert that case instance has variable in <b>history</b>.
     *
     * @param variableName variable to check.
     * @return Historic case instance assertion
     */

    public HistoricCaseInstanceAssert hasVariable(String variableName) {
        caseExistsInHistory();

        if (getCmmnHistoryService().createHistoricCaseInstanceQuery().caseInstanceId(actual.getId()).variableExists(variableName).count() != 1) {
            failWithMessage(getCaseDescription(actual)+" has variable <%s> but variable does not exist in history.", variableName);
        }

        return this;
    }

    /**
     * Assert that case instance does not have variable in <b>history</b>.
     * @param variableName variable to check
     * @return Historic case instance assertion
     */
    public HistoricCaseInstanceAssert doesNotHaveVariable(String variableName) {
        caseExistsInHistory();

        if (getCmmnHistoryService().createHistoricCaseInstanceQuery().caseInstanceId(actual.getId()).variableExists(variableName).count() != 0) {
            failWithMessage(getCaseDescription(actual)+" does not have variable <%s> but variable exists in history.", variableName);
        }

        return this;
    }

    /**
     * Assert that case instance has variable in <b>history</b> with value equals to expectedValue.
     *
     * @param variableName variable to check.
     * @param expectedValue expected variable value.
     * @return Historic case instance assertion
     */
    public HistoricCaseInstanceAssert hasVariableWithValue(String variableName, Object expectedValue) {
        caseExistsInHistory();
        hasVariable(variableName);

        HistoricVariableInstance actualVariable = getCmmnHistoryService().createHistoricVariableInstanceQuery().caseInstanceId(actual.getId()).variableName(variableName).singleResult();
        Assertions.assertThat(actualVariable.getValue()).isEqualTo(expectedValue);
        return this;
    }

    /**
     * Assert list of <b>historic</b> identity links without ordering.
     *
     * @return Assertion of #{@link IdentityLink} list.
     */
    public ListAssert<HistoricIdentityLink> identityLinks() {
        caseExistsInHistory();

        return assertThat(getCmmnHistoryService().getHistoricIdentityLinksForCaseInstance(actual.getId()));
    }

    /**
     * Assert list of user tasks in the <b>history</b> ordered by the task name ascending.
     * Case, Task variables and identityLinks are included.
     *
     * @return Assertion of {@link HistoricTaskInstance} list.
     */
    public ListAssert<HistoricTaskInstance> userTasks() {
        caseExistsInHistory();

        return assertThat(getCmmnHistoryService().createHistoricTaskInstanceQuery().caseInstanceId(actual.getId()).orderByTaskName().asc()
                .includeCaseVariables().includeIdentityLinks().includeTaskLocalVariables().list());
    }

    private void caseExistsInHistory() {
        isNotNull();
        isInHistory();
    }

    private void isInHistory() {
        if (getCmmnHistoryService().createHistoricCaseInstanceQuery().caseInstanceId(actual.getId()).count() != 1) {
            failWithMessage(getCaseDescription(actual)+"> exists in history but case instance not found.");
        }
    }

}
