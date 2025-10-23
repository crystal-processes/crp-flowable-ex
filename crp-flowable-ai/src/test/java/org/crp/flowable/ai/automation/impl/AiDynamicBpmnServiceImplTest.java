package org.crp.flowable.ai.automation.impl;

import org.crp.flowable.ai.automation.AiAutomationTest;
import org.crp.flowable.ai.automation.AiDynamicBpmnService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.junit.jupiter.api.Test;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;

@AiAutomationTest
class AiDynamicBpmnServiceImplTest {

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void addOneServiceTaskToProcess(RuntimeService runtimeService, TaskService taskService,
                                    AiDynamicBpmnService aiDynamicBpmnService) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().singleResult();
        aiDynamicBpmnService.createDynamicServiceTaskBuilder()
                .resultVariableName("output")
                .id("dynamicServiceTask1")
                .expression("${1+1}")
                .injectIntoExecution(execution.getId());

        completeUserTask(taskService, processInstance);

        assertThat(processInstance).inHistory().isFinished()
                .hasVariableWithValue("output", 2L);
    }

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void addUserTaskToProcess(RuntimeService runtimeService, TaskService taskService,
                                    AiDynamicBpmnService aiDynamicBpmnService) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().singleResult();
        aiDynamicBpmnService.createDynamicUserTaskBuilder()
                .id("dynamicUserTask1")
                .name("newlyAddedUserTask")
                .injectIntoExecution(execution.getId());

        completeUserTask(taskService, processInstance);

        assertThat(processInstance).isRunning()
                .userTasks().extracting(TaskInfo::getTaskDefinitionKey).containsOnly("dynamicUserTask1");

        completeUserTask(taskService, processInstance);

        assertThat(processInstance).inHistory().isFinished()
                .activities().extracting(HistoricActivityInstance::getActivityId).containsExactlyInAnyOrder(
                        "dynamicUserTask1",
                        "_flow_theTask__dynamicUserTask1",
                        "theTask",
                        "theStart-theTask",
                        "theStart");

    }

    private static void completeUserTask(TaskService taskService, ProcessInstance processInstance) {
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
    }
}