package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.crp.flowable.mcp.test.CrpMcpTest;
import org.springframework.beans.factory.annotation.Autowired;

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

        assertThat(developerService.maxVariablesPerProcessDefinition())
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
                developerService.variableTypes(processDefinitionKey, types)
        )
                .as(description)
                .contains(expectedResult);
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