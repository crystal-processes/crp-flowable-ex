package org.crp.flowable.ai.automation;

import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@AiAutomationTest
class AiAutomationDelegateTest {

    ChatClient chatClient = mock(ChatClient.class);
    ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);
    Advisor advisor = mock(Advisor.class);

    @AfterEach
    void resetMocks() {
        reset(response, requestSpec, chatClient);
    }

    @BeforeEach
    void initializeMocks(ProcessEngineConfiguration processEngineConfiguration) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(response);
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/automation/oneAiTask.bpmn20.xml")
    void addOneUserTaskToProcess(RuntimeService runtimeService) {
        UserTask userTaskToInject = new UserTask();
        userTaskToInject.setAssignee("injectedAssignee");
        userTaskToInject.setId("injectedTaskId");
        userTaskToInject.setName("injected User Task");
        when(response.entity(UserTask.class))
                .thenReturn(userTaskToInject);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("oneAiTaskProcess")
                .transientVariable("chatClient", chatClient)
                .start();

        assertThat(processInstance).userTasks()
                .as("Newly injected user task")
                .hasSize(1)
                .allMatch( t ->
                        t.getAssignee().equals("injectedAssignee") &&
                        t.getName().equals("injected User Task") &&
                        t.getTaskDefinitionKey().equals("injectedTaskId"));
    }


    @Test
    @Deployment(resources = "org/crp/flowable/ai/automation/oneAiTask.bpmn20.xml")
    void executeProcessWithoutChange(RuntimeService runtimeService) {
        when(response.entity(UserTask.class))
                .thenReturn(null);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("oneAiTaskProcess")
                .transientVariable("chatClient", chatClient)
                .start();

        assertThat(processInstance)
                .as("Process finished without any change.")
                .inHistory()
                .isFinished();
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/automation/oneAiTask.bpmn20.xml")
    void executeProcessWithoutChangeWithEmptyTask(RuntimeService runtimeService) {
        when(response.entity(UserTask.class))
                .thenReturn(new UserTask());
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("oneAiTaskProcess")
                .transientVariable("chatClient", chatClient)
                .start();

        assertThat(processInstance)
                .as("Process finished without any change.")
                .inHistory()
                .isFinished();
    }

}