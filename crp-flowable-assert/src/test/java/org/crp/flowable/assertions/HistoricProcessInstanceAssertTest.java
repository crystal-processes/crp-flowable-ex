package org.crp.flowable.assertions;

import org.assertj.core.groups.Tuple;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.TestUtils.createOneTaskProcess;

@FlowableTest
class HistoricProcessInstanceAssertTest {

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void isFinishedForFinishedProcessInstance(RuntimeService runtimeService, TaskService taskService, HistoryService historyService) {
        ProcessInstance oneTaskProcess = createOneTaskProcess(runtimeService);

        assertThat(oneTaskProcess).inHistory().activities().extracting(HistoricActivityInstance::getActivityId).contains(
                        "theStart", "theStart-theTask", "theTask"
                );

        taskService.complete(taskService.createTaskQuery().processInstanceId(oneTaskProcess.getId()).singleResult().getId());

        assertThat(oneTaskProcess).inHistory().isFinished()
            .activities().extracting(HistoricActivityInstance::getActivityId).contains(
                "theStart", "theStart-theTask", "theTask", "theTask-theEnd", "theEnd"
            );

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(oneTaskProcess.getId()).singleResult();
        assertThat(historicProcessInstance).isFinished()
                .activities().extracting(HistoricActivityInstance::getActivityId).contains(
                        "theStart", "theStart-theTask", "theTask", "theTask-theEnd", "theEnd"
                );
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void variables(RuntimeService runtimeService) {
        ProcessInstance oneTaskProcess = createOneTaskProcess(runtimeService);

        assertThat(oneTaskProcess).as("No variable exists in the process scope.")
                .inHistory().variables().isEmpty();
        
        runtimeService.setVariable(oneTaskProcess.getId(), "testVariable", "variableValue");

        assertThat(oneTaskProcess).as("Variable exists in the process scope, the variable must be present in the history.")
                .inHistory()
                .hasVariable("testVariable")
                .hasVariableWithValue("testVariable", "variableValue")
                .variables().hasSize(1).extracting("name", "value").
                containsExactly(Tuple.tuple("testVariable", "variableValue"));
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void hasVariable(RuntimeService runtimeService) {
        ProcessInstance oneTaskProcess = createOneTaskProcess(runtimeService);

        assertThat(oneTaskProcess).as("No variable exists in the process scope.")
                .inHistory().variables().isEmpty();

        runtimeService.setVariable(oneTaskProcess.getId(), "testVariable", "variableValue");

        assertThat(oneTaskProcess).as("Variable exists in the process scope, the variable must be present in the history.")
                .inHistory().variables().hasSize(1).extracting("name", "value").
                containsExactly(Tuple.tuple("testVariable", "variableValue"));
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void doesNotHaveVariable(RuntimeService runtimeService) {
        ProcessInstance oneTaskProcess = createOneTaskProcess(runtimeService);

        assertThat(oneTaskProcess).as("No variable exists in the process scope.")
                .inHistory().doesNotHaveVariable("nonExistingVariable");

        runtimeService.setVariable(oneTaskProcess.getId(), "testVariable", "variableValue");

        assertThat(oneTaskProcess).as("Variable exists in the process scope, the variable must be present in the history.")
                .inHistory().doesNotHaveVariable("nonExistingVariable")
                .hasVariable("testVariable");

        assertThatThrownBy(() -> assertThat(oneTaskProcess).inHistory().doesNotHaveVariable("testVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+oneTaskProcess.getId()+"> does not have variable <testVariable> but variable exists in history.");
    }

}