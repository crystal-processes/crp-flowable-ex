import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;

@FlowableTest
class jUnitTest {

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
