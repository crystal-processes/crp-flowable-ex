

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
class OneTaskProcessTest {

	@Test
	void case1(RuntimeService runtimeService, TaskService taskService) {
	    ProcessInstance processInstance = null;
	    try {
            // start process instance oneTaskProcess
            processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTaskProcess").start();
            assertThat(processInstance).isNotNull();

            // complete task oneTask
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).
                taskDefinitionKey("sid-D1E992D9-4806-408B-8589-9D56DF95E121").
                singleResult();
            assertThat(task.getName()).isEqualTo("oneTask");
            taskService.complete(task.getId());
        } finally {
            if (processInstance != null) {
                runtimeService.deleteProcessInstance(processInstance.getId(), "test cleanup");
            }
        }
	}

}
