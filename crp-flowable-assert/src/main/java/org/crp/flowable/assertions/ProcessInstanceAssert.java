package org.crp.flowable.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;

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

}
