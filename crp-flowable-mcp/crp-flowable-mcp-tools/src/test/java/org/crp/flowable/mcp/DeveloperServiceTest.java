package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.crp.flowable.mcp.service.DeveloperService.DeadLetterJob;
import org.crp.flowable.mcp.service.DeveloperService.DeadLetterJobDetail;
import org.crp.flowable.mcp.service.DeveloperService.FailingRuntimeJob;
import org.crp.flowable.mcp.service.DeveloperService.LongRunningTransaction;
import org.crp.flowable.mcp.service.DeveloperService.MaxVariableCount;
import org.crp.flowable.mcp.service.DeveloperService.VariableInfo;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.test.JobTestHelper;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.job.api.Job;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.tuple;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;

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
                .addClasspathResource("longRunningLoop.bpmn20.xml")
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

        List<MaxVariableCount> result = developerService.maxVariablesPerProcessDefinition(null, null, null);
        assertThat(result)
                .hasSizeGreaterThan(0)
                .extracting(MaxVariableCount::key)
                .contains("oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithDefinitionKey() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        runtimeService.createProcessInstanceBuilder().processDefinitionKey("failingServiceTask")
                .variable("otherVariable", "otherValue")
                .start();

        List<MaxVariableCount> resultOneTask = developerService.maxVariablesPerProcessDefinition("oneTask", null, null);
        assertThat(resultOneTask)
                .as("definitionKey=oneTask filters to only oneTask process")
                .extracting(MaxVariableCount::key)
                .contains("oneTask")
                .doesNotContain("failingServiceTask");

        List<MaxVariableCount> resultFailingTask = developerService.maxVariablesPerProcessDefinition("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask process")
                .extracting(MaxVariableCount::key)
                .contains("failingServiceTask")
                .doesNotContain("oneTask");

        List<MaxVariableCount> resultNonExisting = developerService.maxVariablesPerProcessDefinition("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .isEmpty();
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithStartedAfter() {
        Instant beforeProcessStart = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();
        Instant afterProcessStart = processInstance.getStartTime().toInstant().plusMillis(1L);

        List<MaxVariableCount> resultBefore = developerService.maxVariablesPerProcessDefinition(null, beforeProcessStart, null);
        assertThat(resultBefore)
                .as("startedAfter before process creation includes the process")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");

        List<MaxVariableCount> resultAfter = developerService.maxVariablesPerProcessDefinition(null, afterProcessStart, null);
        assertThat(resultAfter)
                .as("startedAfter after process creation excludes the process")
                .isEmpty();

        List<MaxVariableCount> resultNull = developerService.maxVariablesPerProcessDefinition(null, null, null);
        assertThat(resultNull)
                .as("null startedAfter includes all processes")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");
    }

    @Test
    public void maxVariablesPerProcessDefinitionWithLatestDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        List<MaxVariableCount> result1 = developerService.maxVariablesPerProcessDefinition(null, null, 1);
        assertThat(result1)
                .as("latestDeployments=1 includes current deployment")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");

        List<MaxVariableCount> result0 = developerService.maxVariablesPerProcessDefinition(null, null, 0);
        assertThat(result0)
                .as("latestDeployments=0 includes all deployments")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");

        List<MaxVariableCount> resultNeg1 = developerService.maxVariablesPerProcessDefinition(null, null, -1);
        assertThat(resultNeg1)
                .as("latestDeployments=-1 includes all deployments")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");

        List<MaxVariableCount> resultNull = developerService.maxVariablesPerProcessDefinition(null, null, null);
        assertThat(resultNull)
                .as("null latestDeployments includes all deployments")
                .extracting(MaxVariableCount::key)
                .contains("oneTask");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("variableTestCases")
    public void findVariables(String description, String definitionKey, Set<String> types, boolean expectEmpty) {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        List<VariableInfo> result = developerService.variableTypes(definitionKey, types, null, null);
        
        if (expectEmpty) {
            assertThat(result).as(description).isEmpty();
        } else {
            assertThat(result)
                    .as(description)
                    .isNotEmpty()
                    .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                    .contains(tuple("string", "testVariable", "oneTask"));
        }
    }

    @Test
    public void variableTypesWithStartedAfter() {
        Instant beforeProcessStart = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();
        Instant afterProcessStart = processInstance.getStartTime().toInstant().plusMillis(1L);

        List<VariableInfo> resultBefore = developerService.variableTypes(null, null, beforeProcessStart, null);
        assertThat(resultBefore)
                .as("startedAfter before process creation includes the process")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));

        List<VariableInfo> resultAfter = developerService.variableTypes(null, null, afterProcessStart, null);
        assertThat(resultAfter)
                .as("startedAfter after process creation excludes the process")
                .isEmpty();

        List<VariableInfo> resultNull = developerService.variableTypes(null, null, null, null);
        assertThat(resultNull)
                .as("null startedAfter includes all processes")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));
    }

    @Test
    public void variableTypesWithLatestDeployments() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        List<VariableInfo> result1 = developerService.variableTypes(null, null, null, 1);
        assertThat(result1)
                .as("latestDeployments=1 includes current deployment")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));

        List<VariableInfo> result0 = developerService.variableTypes(null, null, null, 0);
        assertThat(result0)
                .as("latestDeployments=0 includes all deployments")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));

        List<VariableInfo> resultNeg1 = developerService.variableTypes(null, null, null, -1);
        assertThat(resultNeg1)
                .as("latestDeployments=-1 includes all deployments")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));

        List<VariableInfo> resultNull = developerService.variableTypes(null, null, null, null);
        assertThat(resultNull)
                .as("null latestDeployments includes all deployments")
                .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                .contains(tuple("string", "testVariable", "oneTask"));
    }

    @Test
    public void variableTypesWithDefinitionKey() {
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        runtimeService.createProcessInstanceBuilder().processDefinitionKey("failingServiceTask")
                .variable("otherVariable", "otherValue")
                .start();

        List<VariableInfo> resultOneTask = developerService.variableTypes("oneTask", null, null, null);
        assertThat(resultOneTask)
                .as("definitionKey=oneTask filters to only oneTask variables")
                .extracting(VariableInfo::key, VariableInfo::name, VariableInfo::type)
                .containsExactlyInAnyOrder(
                        tuple("oneTask", "testVariable", "string")
                );

        List<VariableInfo> resultFailingTask = developerService.variableTypes("failingServiceTask", null, null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask variables")
                .extracting(VariableInfo::key, VariableInfo::name, VariableInfo::type)
                .containsExactlyInAnyOrder(
                        tuple("failingServiceTask", "otherVariable", "string")
                );

        List<VariableInfo> resultNonExisting = developerService.variableTypes("nonExistingKey", null, null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .isEmpty();
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

            List<VariableInfo> result1 = developerService.variableTypes(null, null, null, 1);
            assertThat(result1)
                    .as("latestDeployments=1 includes only newest deployment with integer variable")
                    .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                    .contains(tuple("integer", "newVariable", "oneTask"))
                    .doesNotContain(tuple("string", "oldVariable", "oneTask"));

            List<VariableInfo> result2 = developerService.variableTypes(null, null, null, 2);
            assertThat(result2)
                    .as("latestDeployments=2 includes both deployments with string and integer variables")
                    .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                    .contains(
                            tuple("string", "oldVariable", "oneTask"),
                            tuple("integer", "newVariable", "oneTask")
                    );

            List<MaxVariableCount> maxResult1 = developerService.maxVariablesPerProcessDefinition(null, null, 1);
            assertThat(maxResult1)
                    .as("latestDeployments=1 counts only newest deployment variables")
                    .extracting(MaxVariableCount::varCount, MaxVariableCount::key)
                    .contains(tuple(1L, "oneTask"));

            List<MaxVariableCount> maxResult2 = developerService.maxVariablesPerProcessDefinition(null, null, 2);
            assertThat(maxResult2)
                    .as("latestDeployments=2 counts both deployments variables")
                    .extracting(MaxVariableCount::varCount, MaxVariableCount::key)
                    .contains(tuple(1L, "oneTask"));

            List<VariableInfo> result0 = developerService.variableTypes(null, null, null, 0);
            assertThat(result0)
                    .as("latestDeployments=0 includes all deployments like null")
                    .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                    .contains(
                            tuple("string", "oldVariable", "oneTask"),
                            tuple("integer", "newVariable", "oneTask")
                    );

            List<VariableInfo> resultNull = developerService.variableTypes(null, null, null, null);
            assertThat(resultNull)
                    .as("null latestDeployments includes all deployments")
                    .extracting(VariableInfo::type, VariableInfo::name, VariableInfo::key)
                    .contains(
                            tuple("string", "oldVariable", "oneTask"),
                            tuple("integer", "newVariable", "oneTask")
                    );

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    @Test
    public void deadLetterJobs() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        List<DeadLetterJob> result = developerService.deadLetterJobs(null, null, null);

        assertThat(result)
                .as("deadLetterJobs returns dead letter job information")
                .isNotEmpty()
                .extracting(DeadLetterJob::key_, DeadLetterJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));

        List<DeadLetterJob> resultWithFilter = developerService.deadLetterJobs(null, null, 1);

        assertThat(resultWithFilter)
                .as("deadLetterJobs with latestDeployments=1 returns filtered results")
                .isNotEmpty()
                .extracting(DeadLetterJob::key_, DeadLetterJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));
    }

    @Test
    public void deadLetterJobsWithMultipleDeployments() {
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

            List<DeadLetterJob> resultLatest1 = developerService.deadLetterJobs(null, null, 1);

            assertThat(resultLatest1)
                    .as("deadLetterJobs with latestDeployments=1 includes only newest deployment")
                    .extracting(DeadLetterJob::processInstanceId)
                    .contains(newProcessInstance.getId())
                    .doesNotContain(oldProcessInstance.getId());

            List<DeadLetterJob> resultLatest2 = developerService.deadLetterJobs(null, null, 2);

            assertThat(resultLatest2)
                    .as("deadLetterJobs with latestDeployments=2 includes both deployments")
                    .extracting(DeadLetterJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

            List<DeadLetterJob> resultLatest0 = developerService.deadLetterJobs(null, null, 0);

            assertThat(resultLatest0)
                    .as("deadLetterJobs with latestDeployments=0 includes all deployments")
                    .extracting(DeadLetterJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

            List<DeadLetterJob> resultNull = developerService.deadLetterJobs(null, null, null);

            assertThat(resultNull)
                    .as("deadLetterJobs with null latestDeployments includes all deployments")
                    .extracting(DeadLetterJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    @Test
    public void deadLetterJobsWithStartedAfter() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        Instant afterJobCreation = Instant.now().plusMillis(1L);

        List<DeadLetterJob> resultBefore = developerService.deadLetterJobs(null, beforeJobCreation, null);
        assertThat(resultBefore)
                .as("startedAfter before job creation includes the dead letter job")
                .isNotEmpty()
                .extracting(DeadLetterJob::key_, DeadLetterJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));

        List<DeadLetterJob> resultAfter = developerService.deadLetterJobs(null, afterJobCreation, null);
        assertThat(resultAfter)
                .as("startedAfter after job creation excludes the dead letter job")
                .isEmpty();

        List<DeadLetterJob> resultNull = developerService.deadLetterJobs(null, null, null);
        assertThat(resultNull)
                .as("null startedAfter includes all dead letter jobs")
                .isNotEmpty()
                .extracting(DeadLetterJob::key_, DeadLetterJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));
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

        List<DeadLetterJob> resultFailingTask = developerService.deadLetterJobs("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask dead letter jobs")
                .isNotEmpty()
                .extracting(DeadLetterJob::key_, DeadLetterJob::processInstanceId)
                .contains(tuple("failingServiceTask", oneTaskInstance.getId()));

        List<DeadLetterJob> resultNonExisting = developerService.deadLetterJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .isEmpty();
    }

    @Test
    public void deadLetterJobDetails() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        List<DeadLetterJobDetail> result = developerService.deadLetterJobDetails(null, null, null, null);

        assertThat(result)
                .as("deadLetterJobDetails returns dead letter job information with stacktrace")
                .isNotEmpty()
                .extracting(DeadLetterJobDetail::key_, DeadLetterJobDetail::processInstanceId, DeadLetterJobDetail::jobId)
                .contains(tuple("failingServiceTask", processInstance.getId(), result.get(0).jobId()));
        
        assertThat(result.get(0).exceptionMessage())
                .as("exceptionMessage contains expected error")
                .contains("Unknown property used in expression: ${UnableToResolveExpression}");
        
        assertThat(result.get(0).exceptionStacktrace())
                .as("exceptionStacktrace contains full stacktrace")
                .contains("org.flowable.common.engine.api.FlowableException: Unknown property used in expression: ${UnableToResolveExpression}");
    }

    @Test
    public void deadLetterJobDetailsWithJobId() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        Job deadLetterJob = managementService.createDeadLetterJobQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        List<DeadLetterJobDetail> result = developerService.deadLetterJobDetails(deadLetterJob.getId(), null, null, null);

        assertThat(result)
                .as("deadLetterJobDetails with jobId filters to only that job")
                .isNotEmpty()
                .extracting(DeadLetterJobDetail::jobId, DeadLetterJobDetail::key_, DeadLetterJobDetail::processInstanceId)
                .contains(tuple(deadLetterJob.getId(), "failingServiceTask", processInstance.getId()));
        
        assertThat(result.get(0).exceptionStacktrace())
                .as("exceptionStacktrace is present")
                .isNotNull();
    }

    @Test
    public void deadLetterJobDetailsWithDefinitionKey() {
        ProcessInstance oneTaskInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(oneTaskInstance.getId()).count() > 0);

        List<DeadLetterJobDetail> resultFailingTask = developerService.deadLetterJobDetails(null, "failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask dead letter job details")
                .isNotEmpty()
                .extracting(DeadLetterJobDetail::key_, DeadLetterJobDetail::processInstanceId)
                .contains(tuple("failingServiceTask", oneTaskInstance.getId()));

        List<DeadLetterJobDetail> resultNonExisting = developerService.deadLetterJobDetails(null, "nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .isEmpty();
    }

    @Test
    public void deadLetterJobDetailsWithStartedAfter() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        Instant afterJobCreation = Instant.now().plusMillis(1L);

        List<DeadLetterJobDetail> resultBefore = developerService.deadLetterJobDetails(null, null, beforeJobCreation, null);
        assertThat(resultBefore)
                .as("startedAfter before job creation includes the dead letter job")
                .isNotEmpty()
                .extracting(DeadLetterJobDetail::key_, DeadLetterJobDetail::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));

        List<DeadLetterJobDetail> resultAfter = developerService.deadLetterJobDetails(null, null, afterJobCreation, null);
        assertThat(resultAfter)
                .as("startedAfter after job creation excludes the dead letter job")
                .isEmpty();

        List<DeadLetterJobDetail> resultNull = developerService.deadLetterJobDetails(null, null, null, null);
        assertThat(resultNull)
                .as("null startedAfter includes all dead letter job details")
                .isNotEmpty()
                .extracting(DeadLetterJobDetail::key_, DeadLetterJobDetail::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));
    }

    @Test
    public void deadLetterJobDetailsWithLatestDeployments() {
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

            List<DeadLetterJobDetail> resultLatest1 = developerService.deadLetterJobDetails(null, null, null, 1);

            assertThat(resultLatest1)
                    .as("deadLetterJobDetails with latestDeployments=1 includes only newest deployment")
                    .extracting(DeadLetterJobDetail::processInstanceId)
                    .contains(newProcessInstance.getId())
                    .doesNotContain(oldProcessInstance.getId());

            List<DeadLetterJobDetail> resultLatest2 = developerService.deadLetterJobDetails(null, null, null, 2);

            assertThat(resultLatest2)
                    .as("deadLetterJobDetails with latestDeployments=2 includes both deployments")
                    .extracting(DeadLetterJobDetail::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    @Test
    public void failingRuntimeJobs() {
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

        List<FailingRuntimeJob> result = developerService.failingRuntimeJobs(null, null, null);

        assertThat(result)
                .as("failingRuntimeJobs returns failing runtime job information")
                .isNotEmpty()
                .extracting(FailingRuntimeJob::key_, FailingRuntimeJob::processInstanceId, FailingRuntimeJob::retries)
                .contains(tuple("failingServiceTask", processInstance.getId(), result.get(0).retries()));
    }

    @Test
    public void failingRuntimeJobsWithStartedAfter() {
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

        List<FailingRuntimeJob> resultBefore = developerService.failingRuntimeJobs(null, beforeJobCreation, null);
        assertThat(resultBefore)
                .as("startedAfter before job creation includes the failing runtime job")
                .isNotEmpty()
                .extracting(FailingRuntimeJob::key_, FailingRuntimeJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));

        List<FailingRuntimeJob> resultAfter = developerService.failingRuntimeJobs(null, afterJobCreation, null);
        assertThat(resultAfter)
                .as("startedAfter after job creation excludes the failing runtime job")
                .isEmpty();

        List<FailingRuntimeJob> resultNull = developerService.failingRuntimeJobs(null, null, null);
        assertThat(resultNull)
                .as("null startedAfter includes all failing runtime jobs")
                .isNotEmpty()
                .extracting(FailingRuntimeJob::key_, FailingRuntimeJob::processInstanceId)
                .contains(tuple("failingServiceTask", processInstance.getId()));
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

        List<FailingRuntimeJob> resultFailingTask = developerService.failingRuntimeJobs("failingServiceTask", null, null);
        assertThat(resultFailingTask)
                .as("definitionKey=failingServiceTask filters to only failingServiceTask runtime jobs")
                .isNotEmpty()
                .extracting(FailingRuntimeJob::key_, FailingRuntimeJob::processInstanceId)
                .contains(tuple("failingServiceTask", oneTaskInstance.getId()));

        List<FailingRuntimeJob> resultNonExisting = developerService.failingRuntimeJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing definitionKey returns empty results")
                .isEmpty();
    }

    @Test
    public void failingRuntimeJobsWithMultipleDeployments() {
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
            List<FailingRuntimeJob> resultLatest1 = developerService.failingRuntimeJobs(null, null, 1);

            assertThat(resultLatest1)
                    .as("failingRuntimeJobs with latestDeployments=1 includes only newest deployment")
                    .extracting(FailingRuntimeJob::processInstanceId)
                    .contains(newProcessInstance.getId())
                    .doesNotContain(oldProcessInstance.getId());

            // Test with latestDeployments=2 - should include both deployments
            List<FailingRuntimeJob> resultLatest2 = developerService.failingRuntimeJobs(null, null, 2);

            assertThat(resultLatest2)
                    .as("failingRuntimeJobs with latestDeployments=2 includes both deployments")
                    .extracting(FailingRuntimeJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

            // Test with latestDeployments=0 - should include all deployments like null
            List<FailingRuntimeJob> resultLatest0 = developerService.failingRuntimeJobs(null, null, 0);

            assertThat(resultLatest0)
                    .as("failingRuntimeJobs with latestDeployments=0 includes all deployments")
                    .extracting(FailingRuntimeJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

            // Test with null latestDeployments - should include all deployments
            List<FailingRuntimeJob> resultNull = developerService.failingRuntimeJobs(null, null, null);

            assertThat(resultNull)
                    .as("failingRuntimeJobs with null latestDeployments includes all deployments")
                    .extracting(FailingRuntimeJob::processInstanceId)
                    .contains(oldProcessInstance.getId(), newProcessInstance.getId());

        } finally {
            repositoryService.deleteDeployment(newDeployment.getId(), true);
        }
    }

    @Test
    public void longRunningLoop() {
        long threshold = 10;
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", threshold)
                .start();

        assertThat(processInstance)
                .as("index should reach the threshold")
                .hasVariableWithValue("index", threshold);

        List<LongRunningTransaction> result1 = developerService.longRunningTransaction(null, 1);
        assertThat(result1)
                .extracting(LongRunningTransaction::actId, LongRunningTransaction::key, LongRunningTransaction::transactionOrder)
                .contains(tuple("receiveTask", "longRunningLoop", 43L));
        
        List<LongRunningTransaction> resultNull = developerService.longRunningTransaction(null, null);
        assertThat(resultNull)
                .extracting(LongRunningTransaction::actId, LongRunningTransaction::key, LongRunningTransaction::transactionOrder)
                .contains(tuple("receiveTask", "longRunningLoop", 43L));
        
        List<LongRunningTransaction> result2 = developerService.longRunningTransaction(null, 2);
        assertThat(result2)
                .extracting(LongRunningTransaction::actId, LongRunningTransaction::key, LongRunningTransaction::transactionOrder)
                .contains(tuple("receiveTask", "longRunningLoop", 43L));
        
        List<LongRunningTransaction> resultNonExisting = developerService.longRunningTransaction("nonExistingKey", null);
        assertThat(resultNonExisting).isEmpty();
        
        List<LongRunningTransaction> resultFiltered = developerService.longRunningTransaction("longRunningLoop", null);
        assertThat(resultFiltered)
                .extracting(LongRunningTransaction::actId, LongRunningTransaction::key, LongRunningTransaction::transactionOrder)
                .contains(tuple("receiveTask", "longRunningLoop", 43L));
    }

    @Test
    public void longRunningLoopSelectOnlyMax() {
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", 10)
                .start();
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", 12)
                .start();

        List<LongRunningTransaction> result = developerService.longRunningTransaction(null, null);
        assertThat(result)
                .extracting(LongRunningTransaction::transactionOrder)
                .contains(51L)
                .doesNotContain(43L);
    }

    @Test
    public void longRunningLoopInDifferentDeployments() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", 10)
                .start();

        Deployment deployment1 = repositoryService.createDeployment()
                .addClasspathResource("longRunningLoop.bpmn20.xml")
                .deploy();

        ProcessInstance processInstance2 = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", 12)
                .start();

        try {
            List<LongRunningTransaction> result = developerService.longRunningTransaction(null, 1);
            assertThat(result)
                    .extracting(LongRunningTransaction::actId, LongRunningTransaction::key, LongRunningTransaction::transactionOrder, LongRunningTransaction::procDefId)
                    .contains(tuple("receiveTask", "longRunningLoop", 51L, processInstance2.getProcessDefinitionId()))
                    .doesNotContain(tuple(null, null, null, processInstance.getProcessDefinitionId()));
        } finally {
            repositoryService.deleteDeployment(deployment1.getId(), true);
        }
    }

    private static Stream<Arguments> variableTestCases() {
        return Stream.of(
                Arguments.of("null parameters provides all", null, null, false),
                Arguments.of("empty types provides all", null, Set.of(), false),
                Arguments.of("exact types provides limited results", null, Set.of("string"), false),
                Arguments.of("non existing types provides empty results", null, Set.of("NON EXISTING TYPE"), true),
                Arguments.of("non existing key and types provides empty results", "NON EXISTING KEY", Set.of("NON EXISTING TYPE"), true),
                Arguments.of("non existing key and empty types provides empty results", "NON EXISTING KEY", Set.of(), true),
                Arguments.of("exact key provides and empty types limited results", "oneTask", Set.of(), false),
                Arguments.of("exact key and types provides limited results", "oneTask", Set.of("string"), false)
        );
    }
} 
