package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.test.JobTestHelper;
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
    private DeveloperService developerService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    private Deployment deployment;

    @BeforeEach
    void deployOneTaskProcess() {
        deployment = repositoryService.createDeployment()
                .addClasspathResource("oneTask.bpmn20.xml")
                .addClasspathResource("failingServiceTask.bpmn20.xml")
                .deploy();
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

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, null))
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithDefinitionKey() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        runtimeService.createProcessInstanceBuilder().processDefinitionKey("failingServiceTask")
                .variable("otherVariable", "otherValue")
                .start();

        String resultOneTask = developerService.maxVariablesPerProcessDefinition("oneTask", null, null);
        assertThat(resultOneTask)
                .as("definitionKey=oneTask filters to only oneTask process")
                .contains("KEY_=oneTask")
                .contains("MAX_VAR_COUNT=1")
                .doesNotContain("KEY_=failingServiceTask");

        String resultFailingTask = developerService.maxVariablesPerProcessDefinition("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask process")
                .contains("KEY_=failingServiceTask")
                .contains("MAX_VAR_COUNT=1")
                .doesNotContain("KEY_=oneTask");

        String resultNonExisting = developerService.maxVariablesPerProcessDefinition("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .contains("[]");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithStartedAfter() {
        Instant beforeProcessStart = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();
        Instant afterProcessStart = processInstance.getStartTime().toInstant().plusMillis(1L);

        assertThat(developerService.maxVariablesPerProcessDefinition(null, beforeProcessStart, null))
                .as("startedAfter before process creation includes the process")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, afterProcessStart, null))
                .as("startedAfter after process creation excludes the process")
                .contains("[]");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, null))
                .as("null startedAfter includes all processes")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithLatestDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, 1))
                .as("latestDeployments=1 includes current deployment")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, 0))
                .as("latestDeployments=0 includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, -1))
                .as("latestDeployments=-1 includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");

        assertThat(developerService.maxVariablesPerProcessDefinition(null, null, null))
                .as("null latestDeployments includes all deployments")
                .contains("MAX_VAR_COUNT=1")
                .contains("KEY_=oneTask");
    }

    @ParameterizedTest(name = "{1} + {2} => {3}")
    @MethodSource("variableTestCases")
    public void findVariables(String description, String definitionKey, Set<String> types, Collection<String> expectedResult) {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        assertThat(
                developerService.variableTypes(definitionKey, types, null, null)
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
    public void variableTypesWithDefinitionKey() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        runtimeService.createProcessInstanceBuilder().processDefinitionKey("failingServiceTask")
                .variable("otherVariable", "otherValue")
                .start();

        String resultOneTask = developerService.variableTypes("oneTask", null, null, null);
        assertThat(resultOneTask)
                .as("definitionKey=oneTask filters to only oneTask variables")
                .contains("KEY_=oneTask", "NAME_=testVariable", "TYPE_=string")
                .doesNotContain("NAME_=otherVariable");

        String resultFailingTask = developerService.variableTypes("failingServiceTask", null, null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask variables")
                .contains("KEY_=failingServiceTask", "NAME_=otherVariable", "TYPE_=string")
                .doesNotContain("NAME_=testVariable");

        String resultNonExisting = developerService.variableTypes("nonExistingKey", null, null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .contains("[]");
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

            assertThat(developerService.maxVariablesPerProcessDefinition(null, null, 1))
                    .as("latestDeployments=1 counts only newest deployment variables")
                    .contains("MAX_VAR_COUNT=1", "KEY_=oneTask");

            assertThat(developerService.maxVariablesPerProcessDefinition(null, null, 2))
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

    @Test
    public void deadLetterJobsTest() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        String result = developerService.deadLetterJobs(null, null, null);

        assertThat(result)
                .as("deadLetterJobs returns dead letter job information")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId());

        String resultWithFilter = developerService.deadLetterJobs(null, null, 1);

        assertThat(resultWithFilter)
                .as("deadLetterJobs with latestDeployments=1 returns filtered results")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId());
    }

    @Test
    public void deadLetterJobsWithMultipleDeploymentsTest() {
        ProcessInstance oldProcessInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        Deployment newDeployment = repositoryService.createDeployment()
                .addClasspathResource("failingServiceTask.bpmn20.xml")
                .deploy();

        try {
            ProcessInstance newProcessInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("failingServiceTask")
                    .variable("failedJobRetryTimeCycle", "R1/PT1S")
                    .start();

            JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                    10_000, 500L,
                    () -> managementService.createDeadLetterJobQuery().processInstanceId(newProcessInstance.getId()).count() > 0
            && managementService.createDeadLetterJobQuery().processInstanceId(oldProcessInstance.getId()).count() > 0);

            String resultLatest1 = developerService.deadLetterJobs(null, null, 1);

            assertThat(resultLatest1)
                    .as("deadLetterJobs with latestDeployments=1 includes only newest deployment")
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId())
                    .doesNotContain("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId());

            String resultLatest2 = developerService.deadLetterJobs(null, null, 2);

            assertThat(resultLatest2)
                    .as("deadLetterJobs with latestDeployments=2 includes both deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

            String resultLatest0 = developerService.deadLetterJobs(null, null, 0);

            assertThat(resultLatest0)
                    .as("deadLetterJobs with latestDeployments=0 includes all deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

            String resultNull = developerService.deadLetterJobs(null, null, null);

            assertThat(resultNull)
                    .as("deadLetterJobs with null latestDeployments includes all deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    @Test
    public void deadLetterJobsWithStartedAfterTest() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        Instant afterJobCreation = Instant.now().plusMillis(1L);

        assertThat(developerService.deadLetterJobs(null, beforeJobCreation, null))
                .as("startedAfter before job creation includes the dead letter job")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId());

        assertThat(developerService.deadLetterJobs(null, afterJobCreation, null))
                .as("startedAfter after job creation excludes the dead letter job")
                .contains("[]");

        assertThat(developerService.deadLetterJobs(null, null, null))
                .as("null startedAfter includes all dead letter jobs")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId());
    }

    @Test
    public void deadLetterJobsWithDefinitionKey() {
        ProcessInstance oneTaskInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(oneTaskInstance.getId()).count() > 0);

        String resultFailingTask = developerService.deadLetterJobs("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask dead letter jobs")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + oneTaskInstance.getId());

        String resultNonExisting = developerService.deadLetterJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .contains("[]");
    }

    @Test
    public void failingRuntimeJobsTest() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R100/PT5M")
                .start();

        // Wait for the job to be created and fail (but not become a dead letter yet)
        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                5_000, 500L,
                () -> managementService.createTimerJobQuery()
                        .processInstanceId(processInstance.getId())
                        .withException()
                        .count() > 0);

        String result = developerService.failingRuntimeJobs(null, null, null);

        assertThat(result)
                .as("failingRuntimeJobs returns failing runtime job information")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId())
                .contains("RETRIES_=");
    }

    @Test
    public void failingRuntimeJobsWithStartedAfterTest() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R100/PT5M")
                .start();

        // Wait for the job to be created and fail
        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                5_000, 500L,
                () -> managementService.createTimerJobQuery()
                        .processInstanceId(processInstance.getId())
                        .withException()
                        .count() > 0);

        Instant afterJobCreation = Instant.now().plusMillis(1L);

        assertThat(developerService.failingRuntimeJobs(null, beforeJobCreation, null))
                .as("startedAfter before job creation includes the failing runtime job")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId())
                .contains("RETRIES_=");

        assertThat(developerService.failingRuntimeJobs(null, afterJobCreation, null))
                .as("startedAfter after job creation excludes the failing runtime job")
                .contains("[]");

        assertThat(developerService.failingRuntimeJobs(null, null, null))
                .as("null startedAfter includes all failing runtime jobs")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + processInstance.getId())
                .contains("RETRIES_=");
    }

    @Test
    public void failingRuntimeJobsWithDefinitionKey() {
        ProcessInstance oneTaskInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R100/PT5M")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                5_000, 500L,
                () -> managementService.createTimerJobQuery()
                        .processInstanceId(oneTaskInstance.getId())
                        .withException()
                        .count() > 0);

        String resultFailingTask = developerService.failingRuntimeJobs("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask runtime jobs")
                .contains("KEY_=failingServiceTask")
                .contains("PROCESS_INSTANCE_ID_=" + oneTaskInstance.getId())
                .contains("RETRIES_=");

        String resultNonExisting = developerService.failingRuntimeJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .contains("[]");
    }

    @Test
    public void failingRuntimeJobsWithMultipleDeploymentsTest() {
        ProcessInstance oldProcessInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R100/PT5M")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                5_000, 500L,
                () -> managementService.createTimerJobQuery()
                        .processInstanceId(oldProcessInstance.getId())
                        .withException()
                        .count() > 0);

        Deployment newDeployment = repositoryService.createDeployment()
                .addClasspathResource("failingServiceTask.bpmn20.xml")
                .deploy();

        try {
            ProcessInstance newProcessInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("failingServiceTask")
                    .variable("failedJobRetryTimeCycle", "R100/PT5M")
                    .start();

            JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                    5_000, 500L,
                    () -> managementService.createTimerJobQuery()
                            .processInstanceId(newProcessInstance.getId())
                            .withException()
                            .count() > 0
                    && managementService.createTimerJobQuery()
                            .processInstanceId(oldProcessInstance.getId())
                            .withException()
                            .count() > 0);

            // Test with latestDeployments=1 - should only include new deployment
            String resultLatest1 = developerService.failingRuntimeJobs(null, null, 1);

            assertThat(resultLatest1)
                    .as("failingRuntimeJobs with latestDeployments=1 includes only newest deployment")
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId())
                    .doesNotContain("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId());

            // Test with latestDeployments=2 - should include both deployments
            String resultLatest2 = developerService.failingRuntimeJobs(null, null, 2);

            assertThat(resultLatest2)
                    .as("failingRuntimeJobs with latestDeployments=2 includes both deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

            // Test with latestDeployments=0 - should include all deployments like null
            String resultLatest0 = developerService.failingRuntimeJobs(null,null, 0);

            assertThat(resultLatest0)
                    .as("failingRuntimeJobs with latestDeployments=0 includes all deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

            // Test with null latestDeployments - should include all deployments
            String resultNull = developerService.failingRuntimeJobs(null, null, null);

            assertThat(resultNull)
                    .as("failingRuntimeJobs with null latestDeployments includes all deployments")
                    .contains("PROCESS_INSTANCE_ID_=" + oldProcessInstance.getId())
                    .contains("PROCESS_INSTANCE_ID_=" + newProcessInstance.getId());

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
