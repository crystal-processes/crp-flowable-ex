<#if package??>package ${package.asText()}</#if>

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.TaskService;
import org.flowable.engine.event.EventLogEntry;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskLogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@FlowableTest
class ${className.asText()} {

	@Test
	void case1(RuntimeService runtimeService, TaskService taskService) {
	    ProcessInstance processInstance = null;
	    try {
            // start process instance ${processDefinitionKey.asText()}
            processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("${processDefinitionKey.asText()}").start();
            assertThat(processInstance).isNotNull();
<#list taskActivities.iterator() as taskActivity>

            // complete task ${taskActivity.get("activityName").asText()}
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).
                taskDefinitionKey("${taskActivity.get('activityId').asText()}").
                singleResult();
            assertThat(task.getName()).isEqualTo("${taskActivity.get('activityName').asText()}");
            taskService.complete(task.getId());
</#list>
        } finally {
            if (processInstance != null) {
                runtimeService.deleteProcessInstance(processInstance.getId(), "test cleanup");
            }
        }
	}

}
