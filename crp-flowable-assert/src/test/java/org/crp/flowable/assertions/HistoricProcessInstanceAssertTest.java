package org.crp.flowable.assertions;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.Test;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.TestUtils.createOneTaskProcess;

@FlowableTest
class HistoricProcessInstanceAssertTest {

    @Test
    @Deployment(resources = "oneTask.bpmn20.xml")
    void isFinishedForFinishedProcessInstance(RuntimeService runtimeService, TaskService taskService, HistoryService historyService) {
        ProcessInstance processInstance = createOneTaskProcess(runtimeService);

        assertThat(processInstance).inHistory().activities().extracting(HistoricActivityInstance::getActivityId).contains(
                        "theStart", "theStart-theTask", "theTask"
                );

        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());

        assertThat(processInstance).inHistory().isFinished()
            .activities().extracting(HistoricActivityInstance::getActivityId).contains(
                "theStart", "theStart-theTask", "theTask", "theTask-theEnd", "theEnd"
            );

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(historicProcessInstance).isFinished()
                .activities().extracting(HistoricActivityInstance::getActivityId).contains(
                        "theStart", "theStart-theTask", "theTask", "theTask-theEnd", "theEnd"
                );
    }

}