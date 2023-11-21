package org.crp.flowable.assertions;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.TestUtils.createOneTaskProcess;

@FlowableTest
class ProcessInstanceAssertTest {

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void isRunning(RuntimeService runtimeService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);

        assertThat(processInstance).isRunning();
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void isRunningForNonRunningProcess(RuntimeService runtimeService, TaskService taskService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThatThrownBy(() -> assertThat(processInstance).isRunning())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> to be running but is not.");
    }

    @Test
    void isRunningForNull() {
        assertThatThrownBy(() -> assertThat((ProcessInstance) null).isRunning())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void hasVariable(RuntimeService runtimeService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        runtimeService.setVariable(processInstance.getId(), "variableName", "variableValue");

        assertThat(processInstance).hasVariable("variableName");
        assertThat(processInstance).hasVariable("variableName").isRunning();
        assertThat(processInstance).isRunning().hasVariable("variableName");
        assertThatThrownBy(() -> assertThat(processInstance).hasVariable("nonExistingVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> has variable <nonExistingVariable> but variable does not exist.");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void hasVariableForNonRunningProcess(RuntimeService runtimeService, TaskService taskService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        runtimeService.setVariable(processInstance.getId(), "variableName", "variableValue");
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThatThrownBy(() -> assertThat(processInstance).hasVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> has variable <variableName> but variable does not exist.");
        assertThatThrownBy(() -> assertThat(processInstance).hasVariable("nonExistingVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> has variable <nonExistingVariable> but variable does not exist.");
    }

    @Test
    void hasVariableForNull() {
        assertThatThrownBy(() -> assertThat((ProcessInstance) null).hasVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void doesNotHaveVariable(RuntimeService runtimeService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        runtimeService.setVariable(processInstance.getId(), "variableName", "variableValue");

        assertThat(processInstance).doesNotHaveVariable("NonExistingVariableName");
        assertThat(processInstance).doesNotHaveVariable("NonExistingVariableName").isRunning();
        assertThatThrownBy(() -> assertThat(processInstance).doesNotHaveVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> does not have variable <variableName> but variable exists.");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void doesNotHaveVariableForNonRunningProcess(RuntimeService runtimeService, TaskService taskService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        runtimeService.setVariable(processInstance.getId(), "variableName", "variableValue");
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThat(processInstance).doesNotHaveVariable("variableName");
        assertThat(processInstance).doesNotHaveVariable("nonExistingVariable");
    }

    @Test
    void doesNotHaveVariableForNull() {
        assertThatThrownBy(() -> assertThat((ProcessInstance) null).doesNotHaveVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void doesNotExistForRunningInstance(RuntimeService runtimeService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);

        assertThatThrownBy(() -> assertThat(processInstance).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected process instance <oneTaskProcess, "+processInstance.getId()+"> is finished but instance exists in runtime.");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void doesNotExistForNonRunningProcess(RuntimeService runtimeService, TaskService taskService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThat(processInstance).doesNotExist();
    }

    @Test
    void doesNotExistForNull() {
        assertThatThrownBy(() -> assertThat((ProcessInstance) null).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void activitiesForRunningInstance(RuntimeService runtimeService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);

        assertThat(processInstance).activities().extracting(ActivityInstance::getActivityId)
                .contains("theStart", "theStart->theTask", "theTask");
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void activitiesForNonRunningProcess(RuntimeService runtimeService, TaskService taskService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThat(processInstance).activities().isEmpty();
    }

    @Test
    void activitiesForNull() {
        assertThatThrownBy(() -> assertThat((ProcessInstance) null).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

}