package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.crp.flowable.mcp.test.CrpMcpTest;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CrpMcpTest
public class DeveloperServiceTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DeveloperService developerService;

    private Deployment deployment;

    @BeforeEach
    void deployOneTaskProcess() {
        deployment = repositoryService.createDeployment().addClasspathResource("oneTask.bpmn20.xml").deploy();
    }
    @AfterEach
    void deleteDeployment() {
        repositoryService.deleteDeployment(deployment.getId(), true);
    }

    @Test
    public void maxVariablesPerProcessDefinition() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null))
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithStartedAfter() {
        Instant beforeProcessStart = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();
        Instant afterProcessStart = processInstance.getStartTime().toInstant().plusMillis(1L);

        assertThat(developerService.maxVariablesPerProcessDefinition(beforeProcessStart, null))
                .as("startedAfter before process creation includes the process")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(afterProcessStart, null))
                .as("startedAfter after process creation excludes the process")
                .contains("[]");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null))
                .as("null startedAfter includes all processes")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithLatestDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(developerService.maxVariablesPerProcessDefinition(null, 1))
                .as("latestDeployments=1 includes current deployment")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, 0))
                .as("latestDeployments=0 includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, -1))
                .as("latestDeployments=-1 includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null))
                .as("null latestDeployments includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @ParameterizedTest(name ="{1} + {2} => {3}")
    @MethodSource("variableTestCases")
    public void findVariables(String description, String processDefinitionKey, Set<String> types, Collection<String> expectedResult) {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(
                developerService.variableTypes(processDefinitionKey, types, null, null)
        )
                .as(description)
                .contains(expectedResult);
    }

    @Test
    public void variableTypesWithStartedAfter() {
        Instant beforeProcessStart = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();
        Instant afterProcessStart = processInstance.getStartTime().toInstant().plusMillis(1L);

        assertThat(developerService.variableTypes(null, null, beforeProcessStart, null))
                .as("startedAfter before process creation includes the process")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");

        assertThat(developerService.variableTypes(null, null, afterProcessStart, null))
                .as("startedAfter after process creation excludes the process")
                .contains("[]");

        assertThat(developerService.variableTypes(null, null, null, null))
                .as("null startedAfter includes all processes")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");
    }

    @Test
    public void variableTypesWithLatestDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(developerService.variableTypes(null, null, null, 1))
                .as("latestDeployments=1 includes current deployment")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");

        assertThat(developerService.variableTypes(null, null, null, 0))
                .as("latestDeployments=0 includes all deployments")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");

        assertThat(developerService.variableTypes(null, null, null, -1))
                .as("latestDeployments=-1 includes all deployments")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");

        assertThat(developerService.variableTypes(null, null, null, null))
                .as("null latestDeployments includes all deployments")
                .contains("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask");
    }

    @Test
    public void variableTypesWithMultipleDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("oldVariable", "oldValue")
                .start();

        Deployment newDeployment = repositoryService.createDeployment()
                .addClasspathResource("oneTask.bpmn20.xml")
                .deploy();

        try {
            runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                    .variable("newVariable", 42)
                    .start();

            assertThat(developerService.variableTypes(null, null, null, 1))
                    .as("latestDeployments=1 includes only newest deployment with integer variable")
                    .contains("TYPE_=integer", "NAME_=newVariable", "KEY_=oneTask")
                    .doesNotContain("NAME_=oldVariable");

            assertThat(developerService.variableTypes(null, null, null, 2))
                    .as("latestDeployments=2 includes both deployments with string and integer variables")
                    .contains("TYPE_=string", "NAME_=oldVariable", "TYPE_=integer", "NAME_=newVariable", "KEY_=oneTask");

            assertThat(developerService.maxVariablesPerProcessDefinition(null, 1))
                    .as("latestDeployments=1 counts only newest deployment variables")
                    .contains("MAX_VAR_COUNT=1", "KEY_=oneTask");

            assertThat(developerService.maxVariablesPerProcessDefinition(null, 2))
                    .as("latestDeployments=2 counts both deployments variables")
                    .contains("MAX_VAR_COUNT=1", "KEY_=oneTask");

            assertThat(developerService.variableTypes(null, null, null, 0))
                    .as("latestDeployments=0 includes all deployments like null")
                    .contains("TYPE_=string", "NAME_=oldVariable", "TYPE_=integer", "NAME_=newVariable", "KEY_=oneTask");

            assertThat(developerService.variableTypes(null, null, null, null))
                    .as("null latestDeployments includes all deployments")
                    .contains("TYPE_=string", "NAME_=oldVariable", "TYPE_=integer", "NAME_=newVariable", "KEY_=oneTask");

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    private static Stream<Arguments> variableTestCases() {
        return Stream.of(
                Arguments.of("null parameters provides all", null, null, Set.of("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask")),
                Arguments.of("empty types provides all", null, Set.of(), Set.of("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask")),
                Arguments.of("exact types provides limited results", null, Set.of("string"), Set.of("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask")),
                Arguments.of("non existing types provides empty results", null, Set.of("NON EXISTING TYPE"), Set.of("[]")),
                Arguments.of("non existing key and types provides empty results", "NON EXISTING KEY", Set.of("NON EXISTING TYPE"), Set.of("[]")),

                Arguments.of("non existing key and empty types provides empty results", "NON EXISTING KEY", Set.of(), Set.of("[]")),
                Arguments.of("exact key provides and empty types limited results", "oneTask", Set.of(), Set.of("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask")),
                Arguments.of("exact key and types provides limited results", "oneTask", Set.of("string"), Set.of("TYPE_=string", "NAME_=testVariable", "KEY_=oneTask"))
        );
    }
}