package org.crp.flowable.ai.delegates;

import org.crp.flowable.ai.AiException;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.converter.MapOutputConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@FlowableTest
class ChatClientJavaDelegateTest {

    ChatClient chatClient = mock(ChatClient.class);
    ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);
    Advisor advisor = mock(Advisor.class);

    @AfterEach
    void resetMocks() {
        reset(response, requestSpec, chatClient);
    }

    @BeforeEach
    void initializeMocks() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(response);
        when(response.content()).thenReturn("Hello World!");
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void callChatClient(RuntimeService runtimeService) {
        assertThat(
                createProcessInstanceWithCorrectParameters(runtimeService)
        )
                .hasVariableWithValue("greeting", "Hello World!");
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void callChatClientWithAdvisor(RuntimeService runtimeService) {
        assertThat(
                createProcessInstanceWithAdvisor(runtimeService)
        )
                .hasVariableWithValue("greeting", "Hello World!");
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void isTransientTrue(RuntimeService runtimeService) {
        ProcessInstance processInstance = createChatClientProcessInstanceBuilder(runtimeService)
                .transientVariables(Map.of(
                        "chatClient", chatClient,
                        "system", "You are hello world client. You always answers 'Hello world!'!",
                        "user", "Hello!",
                        "advisors", List.of(),
                        "resultVariableName", "greeting",
                        "isTransient", true
                ))
                .transientVariable("structuredOutputConverter", null)
                .start();

        assertThat(
                processInstance
        )
                .doesNotHaveVariable("greeting");
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void withStructuredMapper(RuntimeService runtimeService) {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        when(response.entity(mapOutputConverter)).thenReturn(Map.of("message", "Hello World!"));
        ProcessInstance processInstance = createChatClientProcessInstanceBuilder(runtimeService)
                .transientVariables(Map.of(
                        "chatClient", chatClient,
                        "system", "You are hello world client. You always answers 'Hello world!'!",
                        "user", "Hello!",
                        "resultVariableName", "greeting",
                        "isTransient", false,
                        "advisors", List.of(),
                        "structuredOutputConverter", mapOutputConverter
                ))
                .start();

        assertThat(
                processInstance
        )
                .hasVariableWithValue("greeting", Map.of("message", "Hello World!"));
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void withoutMandatoryFieldChatClient(RuntimeService runtimeService) {
        assertThatThrownBy(() ->
                createChatClientProcessInstanceBuilder(runtimeService)
                        .transientVariables(Map.of(
                                "system", "You are hello world client. You always answers 'Hello world!'!",
                                "user", "Hello!",
                                "resultVariableName", "greeting",
                                "isTransient", false
                        ))
                        .transientVariable("structuredOutputConverter", null)
                        .transientVariable("chatClient", null)
                        .start()
        ).isInstanceOf(AiException.class)
                .hasMessage("chatClient is mandatory");
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/chatClientCallProcess.bpmn")
    void withoutMandatoryFieldResultVariableName(RuntimeService runtimeService) {
        assertThatThrownBy(() ->
                createChatClientProcessInstanceBuilder(runtimeService)
                        .transientVariables(Map.of(
                                "chatClient", chatClient,
                                "system", "You are hello world client. You always answers 'Hello world!'!",
                                "user", "Hello!",
                                "isTransient", false
                        ))
                        .transientVariable("structuredOutputConverter", null)
                        .transientVariable("resultVariableName", null)
                        .start()
        ).isInstanceOf(AiException.class)
                .hasMessage("ResultVariableName is mandatory");
    }

    @Test
    void withoutChatClientInModel(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                <!--                                            <flowable:field name="chatClient">
                                                                    <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                </flowable:field>
                -->
                                                                             <flowable:field name="system">
                                                                               <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="user">
                                                                               <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="structuredOutputConverter">
                                                                               <flowable:expression><![CDATA[${structuredOutputConverter}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="resultVariableName">
                                                                               <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="isTransient">
                                                                               <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                             </flowable:field>
                """, repositoryService, runtimeService, "chatClient is mandatory");
    }

    @Test
    void withoutResultVariableNameInModel(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                                                                <flowable:field name="chatClient">
                                                                    <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                </flowable:field>
                                                                             <flowable:field name="system">
                                                                               <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="user">
                                                                               <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="structuredOutputConverter">
                                                                               <flowable:expression><![CDATA[${structuredOutputConverter}]]></flowable:expression>
                                                                             </flowable:field>
                  <!--                                                       <flowable:field name="resultVariableName">
                                                                               <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                             </flowable:field>
                  -->
                                                                             <flowable:field name="isTransient">
                                                                               <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                             </flowable:field>
                """, repositoryService, runtimeService, "ResultVariableName is mandatory");
    }

    @Test
    void withoutSystem(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                                                                <flowable:field name="chatClient">
                                                                    <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                </flowable:field>
                <!--                                                             <flowable:field name="system">
                                                                               <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                             </flowable:field>
                -->                                                             <flowable:field name="user">
                                                                               <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="structuredOutputConverter">
                                                                               <flowable:expression><![CDATA[${structuredOutputConverter}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="resultVariableName">
                                                                               <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="isTransient">
                                                                               <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                             </flowable:field>
                """, repositoryService, runtimeService, "system is mandatory");
    }

    @Test
    void withoutUser(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                                                                <flowable:field name="chatClient">
                                                                    <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                </flowable:field>
                                                                             <flowable:field name="system">
                                                                               <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                             </flowable:field>
                <!--                                                             <flowable:field name="user">
                                                                               <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                             </flowable:field>
                -->
                                                                             <flowable:field name="structuredOutputConverter">
                                                                               <flowable:expression><![CDATA[${structuredOutputConverter}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="resultVariableName">
                                                                               <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                             </flowable:field>
                                                                             <flowable:field name="isTransient">
                                                                               <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                             </flowable:field>
                """, repositoryService, runtimeService, "user is mandatory");
    }

    @Test
    void withoutStructuredOutputConverter(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                                                                        <flowable:field name="chatClient">
                                                                            <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                        </flowable:field>
                                                                                     <flowable:field name="system">
                                                                                       <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="user">
                                                                                       <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="structuredOutputConverter">
                                                                                       <flowable:expression><![CDATA[${null}]]></flowable:expression>
                                                                                     </flowable:field>
                        
                                                                                     <flowable:field name="resultVariableName">
                                                                                       <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="isTransient">
                                                                                       <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                                     </flowable:field>
                        """, repositoryService, runtimeService,
                processInstance -> assertThat(processInstance)
                        .hasVariableWithValue("greeting", "Hello World!"));
    }

    @Test
    void withoutIsTransient(RuntimeService runtimeService, RepositoryService repositoryService) throws IOException {
        runModifiedProcessModel("""
                                                                        <flowable:field name="chatClient">
                                                                            <flowable:expression><![CDATA[${chatClient}]]></flowable:expression>
                                                                        </flowable:field>
                                                                                     <flowable:field name="system">
                                                                                       <flowable:expression><![CDATA[${system}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="user">
                                                                                       <flowable:expression><![CDATA[${user}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="structuredOutputConverter">
                                                                                       <flowable:expression><![CDATA[${structuredOutputConverter}]]></flowable:expression>
                                                                                     </flowable:field>
                                                                                     <flowable:field name="resultVariableName">
                                                                                       <flowable:expression><![CDATA[${resultVariableName}]]></flowable:expression>
                                                                                     </flowable:field>
                        <!--                                                             <flowable:field name="isTransient">
                                                                                       <flowable:expression><![CDATA[${isTransient}]]></flowable:expression>
                                                                                     </flowable:field>
                        -->
                        """, repositoryService, runtimeService,
                processInstance -> assertThat(processInstance)
                        .hasVariableWithValue("greeting", "Hello World!"));
    }

    private void runModifiedProcessModel(String replacement, RepositoryService repositoryService, RuntimeService runtimeService, String expectedExceptionMessage) throws IOException {
        String deploymentId = null;
        try (InputStream processResource = getClass().getResourceAsStream("/org/crp/flowable/ai/delegates/chatClientCallProcess-withReplacement.bpmn")) {
            String processModel = new String(Objects.requireNonNull(processResource).readAllBytes()).replace("REPLACEMENT",
                    replacement);
            deploymentId = repositoryService.createDeployment()
                    .addString("chatClientProcess-withoutClient.bpmn", processModel)
                    .deploy().getId();

            assertThatThrownBy(() -> createProcessInstanceWithCorrectParameters(runtimeService))
                    .isInstanceOf(AiException.class)
                    .hasMessage(expectedExceptionMessage);
        } finally {
            if (deploymentId != null) {
                repositoryService.deleteDeployment(deploymentId);
            }
        }
    }

    private void runModifiedProcessModel(String replacement, RepositoryService repositoryService, RuntimeService runtimeService,
                                         Consumer<ProcessInstance> assertion) throws IOException {
        String deploymentId = null;
        try (InputStream processResource = getClass().getResourceAsStream("/org/crp/flowable/ai/delegates/chatClientCallProcess-withReplacement.bpmn")) {
            String processModel = new String(Objects.requireNonNull(processResource).readAllBytes()).replace("REPLACEMENT",
                    replacement);
            deploymentId = repositoryService.createDeployment()
                    .addString("chatClientProcess-withoutClient.bpmn", processModel)
                    .deploy().getId();

            ProcessInstance processInstance = createProcessInstanceWithCorrectParameters(runtimeService);
            assertion.accept(processInstance);
        } finally {
            if (deploymentId != null) {
                repositoryService.deleteDeployment(deploymentId,true);
            }
        }
    }

    private static ProcessInstanceBuilder createChatClientProcessInstanceBuilder(RuntimeService runtimeService) {
        return runtimeService.createProcessInstanceBuilder().processDefinitionKey("chatClientCallProcess");
    }

    private ProcessInstance createProcessInstanceWithCorrectParameters(RuntimeService runtimeService) {
        return createChatClientProcessInstanceBuilder(runtimeService)
                .transientVariables(Map.of(
                        "chatClient", chatClient,
                        "system", "You are hello world client. You always answers 'Hello world!'!",
                        "user", "Hello!",
                        "resultVariableName", "greeting",
                        "advisors", List.of(),
                        "isTransient", false
                ))
                .transientVariable("structuredOutputConverter", null)
                .start();
    }

    private ProcessInstance createProcessInstanceWithAdvisor(RuntimeService runtimeService) {

        return createChatClientProcessInstanceBuilder(runtimeService)
                .transientVariables(Map.of(
                        "chatClient", chatClient,
                        "system", "You are hello world client. You always answers 'Hello world!'!",
                        "user", "Hello!",
                        "resultVariableName", "greeting",
                        "advisors", List.of(advisor),
                        "isTransient", false
                ))
                .transientVariable("structuredOutputConverter", null)
                .start();
    }

}