package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.eventsubscription.api.EventSubscription;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.variable.api.persistence.entity.VariableInstance;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.Utils.*;

public class ProcessInstanceAssert extends AbstractAssert<ProcessInstanceAssert, ProcessInstance> {
    protected ProcessInstanceAssert(ProcessInstance processInstance) {
        super(processInstance, ProcessInstanceAssert.class);
    }

    /**
     * Assert that process instance exists in <b>runtime</b>.
     *
     * @return Process instance assert.
     */
    public ProcessInstanceAssert isRunning() {
        isNotNull();

        if (getRuntimeService().createProcessInstanceQuery().processInstanceId(actual.getId()).count() < 1) {
            failWithMessage(getProcessDescription(actual)+" to be running but is not.", actual.getId());
        }
        return this;
    }

    /**
     * Assert that process instance has variable in <b>runtime</b>.
     *
     * @param variableName variable to check.
     * @return Process instance assertion
     */
    public ProcessInstanceAssert hasVariable(String variableName) {
        isNotNull();

        if (getRuntimeService().createProcessInstanceQuery().processInstanceId(actual.getId()).variableExists(variableName).count() != 1) {
            failWithMessage(getProcessDescription(actual)+" has variable <%s> but variable does not exist.", variableName);
        }

        return this;
    }

    /**
     * Assert that process instance does not have variable in <b>runtime</b>.
     * @param variableName variable to check
     * @return Process instance assertion
     */
    public ProcessInstanceAssert doesNotHaveVariable(String variableName) {
        isNotNull();

        if (getRuntimeService().createProcessInstanceQuery().processInstanceId(actual.getId()).variableExists(variableName).count() != 0) {
            failWithMessage(getProcessDescription(actual)+" does not have variable <%s> but variable exists.", variableName);
        }

        return this;
    }

    /**
     * Assert that process instance does on exist in <b>runtime</b>.
     *
     * @return Process instance assertion
     */
    public ProcessInstanceAssert doesNotExist() {
        isNotNull();

        if (getRuntimeService().createProcessInstanceQuery().processInstanceId(actual.getId()).count() != 0) {
            failWithMessage(getProcessDescription(actual)+" is finished but instance exists in runtime.");
        }

        return this;
    }

    /**
     * @return Historic process instance assertion
     */
    public HistoricProcessInstanceAssert inHistory() {
        return assertThat(getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(actual.getId()).singleResult());
    }

    /**
     * Assert list of <b>runtime</b> process instance activities ordered by activity start time.
     *
     * @return Assertion of #{@link ActivityInstance} list.
     */
    public ListAssert<ActivityInstance> activities() {
        isNotNull();

        return assertThat(getRuntimeService().createActivityInstanceQuery().processInstanceId(actual.getId()).orderByActivityInstanceStartTime().asc().list());
    }

    public ListAssert<Execution> executions() {
        isNotNull();

        return assertThat(getRuntimeService().createExecutionQuery().processInstanceId(actual.getId()).list());
    }

    public ListAssert<VariableInstance> variables() {
        isNotNull();

        return assertThat(getRuntimeService().createVariableInstanceQuery().processInstanceId(actual.getId()).list());
    }

    public ObjectAssert<VariableInstance> variable(String variableName) {
        isNotNull();

        return assertThat(
                getRuntimeService().createVariableInstanceQuery().processInstanceId(actual.getId()).variableName(variableName).singleResult()
        ).isNotNull();
    }

    public ListAssert<IdentityLink> identityLinks() {
        isNotNull();

        return assertThat(getRuntimeService().getIdentityLinksForProcessInstance(actual.getId()));
    }

    public ListAssert<Task> userTasks() {
        isNotNull();

        return assertThat(getTaskService().createTaskQuery().processInstanceId(actual.getId())
                .includeProcessVariables().includeIdentityLinks().includeTaskLocalVariables().list());
    }

    public ListAssert<EventSubscription> eventSubscription() {
        isNotNull();

        return assertThat(getRuntimeService().createEventSubscriptionQuery().processInstanceId(actual.getId()).orderByCreateDate().asc().list());
    }

}
