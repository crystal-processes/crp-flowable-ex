package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.MilestoneInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.api.runtime.UserEventListenerInstance;
import org.flowable.eventsubscription.api.EventSubscription;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.variable.api.persistence.entity.VariableInstance;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.Utils.*;

public class CaseInstanceAssert extends AbstractAssert<CaseInstanceAssert, CaseInstance> {
    protected CaseInstanceAssert(CaseInstance caseInstance) {
        super(caseInstance, CaseInstanceAssert.class);
    }

    /**
     * Assert that case instance exists in <b>runtime</b>.
     *
     * @return Case instance assert.
     */
    public CaseInstanceAssert isRunning() {
        isNotNull();

        if (getCmmnRuntimeService().createCaseInstanceQuery().caseInstanceId(actual.getId()).count() < 1) {
            failWithMessage(getCaseDescription(actual)+" to be running but is not.", actual.getId());
        }
        return this;
    }

    /**
     * Assert that case instance has variable in <b>runtime</b>.
     *
     * @param variableName variable to check.
     * @return Case instance assertion
     */
    public CaseInstanceAssert hasVariable(String variableName) {
        isNotNull();

        if (getCmmnRuntimeService().createCaseInstanceQuery().caseInstanceId(actual.getId()).variableExists(variableName).count() != 1) {
            failWithMessage(getCaseDescription(actual)+" has variable <%s> but variable does not exist.", variableName);
        }

        return this;
    }

    /**
     * Assert that case instance has variable in <b>runtime</b> with value equals to expectedValue.
     *
     * @param variableName variable to check.
     * @param expectedValue expected variable value.
     * @return Case instance assertion
     */
    public CaseInstanceAssert hasVariableWithValue(String variableName, Object expectedValue) {
        isNotNull();
        hasVariable(variableName);

        VariableInstance actualVariable = getCmmnRuntimeService().createVariableInstanceQuery().caseInstanceId(actual.getId()).variableName(variableName).singleResult();
        Assertions.assertThat(actualVariable.getValue()).isEqualTo(expectedValue);
        return this;
    }

    /**
     * Assert that case instance does not have variable in <b>runtime</b>.
     * @param variableName variable to check
     * @return Case instance assertion
     */
    public CaseInstanceAssert doesNotHaveVariable(String variableName) {
        isNotNull();

        if (getCmmnRuntimeService().createCaseInstanceQuery().caseInstanceId(actual.getId()).variableExists(variableName).count() != 0) {
            failWithMessage(getCaseDescription(actual)+" does not have variable <%s> but variable exists.", variableName);
        }

        return this;
    }

    /**
     * Assert that case instance does on exist in <b>runtime</b>.
     *
     * @return Case instance assertion
     */
    public CaseInstanceAssert doesNotExist() {
        isNotNull();

        if (getCmmnRuntimeService().createCaseInstanceQuery().caseInstanceId(actual.getId()).count() != 0) {
            failWithMessage(getCaseDescription(actual)+" is finished but instance exists in runtime.");
        }

        return this;
    }

    /**
     * @return Historic case instance assertion
     */
    public HistoricCaseInstanceAssert inHistory() {
        return assertThat(getCmmnHistoryService().createHistoricCaseInstanceQuery().caseInstanceId(actual.getId()).singleResult());
    }

    /**
     * Assert list of <b>runtime</b> case instance milestones ordered by milestone start time.
     *
     * @return Assertion of #{@link org.flowable.cmmn.api.runtime.MilestoneInstance} list.
     */
    public ListAssert<MilestoneInstance> milestones() {
        isNotNull();

        return assertThat(getCmmnRuntimeService().createMilestoneInstanceQuery().milestoneInstanceCaseInstanceId(actual.getId())
                .orderByTimeStamp().asc().list());
    }

    /**
     * Assert list of <b>runtime</b> execution instances without ordering.
     *
     * @return Assertion of #{@link PlanItemInstance} list.
     */
    public ListAssert<PlanItemInstance> planItems() {
        isNotNull();

        return assertThat(getCmmnRuntimeService().createPlanItemInstanceQuery().caseInstanceId(actual.getId()).list());
    }

    /**
     * Assert list of <b>runtime</b> variable instances ordered by variable name ascending.
     *
     * @return Assertion of #{@link VariableInstance} list.
     */
    public ListAssert<VariableInstance> variables() {
        isNotNull();

        return assertThat(getCmmnRuntimeService().createVariableInstanceQuery().caseInstanceId(actual.getId()).orderByVariableName().asc().list());
    }

    /**
     * Assert list of <b>runtime</b> identity links without ordering.
     *
     * @return Assertion of #{@link IdentityLink} list.
     */
    public ListAssert<IdentityLink> identityLinks() {
        isNotNull();

        return assertThat(getCmmnRuntimeService().getIdentityLinksForCaseInstance(actual.getId()));
    }

    /**
     * Assert list of user tasks in the <b>runtime</b> ordered by the task name ascending.
     *
     * @return Assertion of {@link Task} list.
     */
    public ListAssert<Task> userTasks() {
        isNotNull();

        return assertThat(getCmmnTaskService().createTaskQuery().caseInstanceId(actual.getId()).orderByTaskName().asc()
                .includeCaseVariables().includeIdentityLinks().includeTaskLocalVariables().list());
    }

    /**
     * Assert list of user event subscriptions in the <b>runtime</b> ordered by the event name ascending.
     *
     * @return Assertion of {@link EventSubscription} list.
     */

    public ListAssert<UserEventListenerInstance> userEventListeners() {
        isNotNull();

        return assertThat(getCmmnRuntimeService().createUserEventListenerInstanceQuery().caseInstanceId(actual.getId()).orderByName().asc().list());
    }

}
