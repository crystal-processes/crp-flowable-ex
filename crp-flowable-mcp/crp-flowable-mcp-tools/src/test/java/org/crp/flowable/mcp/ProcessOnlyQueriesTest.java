package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.crp.flowable.mcp.service.DeveloperService.*;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.repository.CmmnDeployment;
import org.flowable.cmmn.api.runtime.CaseInstance;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Tests to verify that all queries return only process-related data (filtering out case data).
 * These tests will fail with LEFT JOIN / INNER JOIN on ACT_RE_PROCDEF only,
 * because case variables don't have a process definition.
 */
@CrpMcpTest
public class ProcessOnlyQueriesTest {

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

    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;
    @Autowired
    private CmmnRepositoryService cmmnRepositoryService;

    private Deployment processDeployment;
    private CmmnDeployment caseDeployment;

    @BeforeEach
    void deployProcessAndCase() {
        // Deploy process
        processDeployment = repositoryService.createDeployment()
                .addClasspathResource("oneTask.bpmn20.xml")
                .addClasspathResource("failingServiceTask.bpmn20.xml")
                .addClasspathResource("longRunningLoop.bpmn20.xml")
                .deploy();

        caseDeployment = cmmnRepositoryService.createDeployment()
                .addClasspathResource("oneHumanTaskCase.cmmn")
                .deploy();
    }

    @AfterEach
    void deleteDeployments() {
        if (caseDeployment != null) {
            cmmnRepositoryService.deleteDeployment(caseDeployment.getId(), true);
        }
        if (processDeployment != null) {
            repositoryService.deleteDeployment(processDeployment.getId(), true);
        }
    }

    /**
     * Tests that maxVariablesPerProcessDefinition returns only process-related data
     * and does NOT include case variables.
     */
    @Test
    public void maxVariablesPerProcessDefinitionReturnsOnlyProcesses() {
        // Create process instance with variable
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        // Create case instance with variable
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();
        
        cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());

        List<MaxVariableCount> result = developerService.maxVariablesPerProcessDefinition(null, null, null);
        
        // All results should have non-null key (only processes, not cases)
        assertThat(result)
                .as("All results should have non-null process definition key")
                .extracting(MaxVariableCount::key)
                .doesNotContainNull();
    }

    /**
     * Tests that variableTypes returns only process-related data
     * and does NOT include case variables.
     */
    @Test
    public void variableTypesReturnsOnlyProcesses() {
        // Create process instance with variable
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        // Create case instance with variable
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();
        
        cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());

        List<VariableInfo> result = developerService.variableTypes(null, null, null, null);
        
        // All results should have non-null key (only processes, not cases)
        assertThat(result)
                .as("All results should have non-null process definition key")
                .extracting(VariableInfo::key)
                .doesNotContainNull();
    }

    /**
     * Tests that variableTypes with types filter returns only process-related data.
     */
    @Test
    public void variableTypesWithTypesReturnsOnlyProcesses() {
        // Create process instance with string variable
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTask")
                .variable("testVariable", "testValue")
                .start();

        // Create case instance with string variable
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();
        
        cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());

        List<VariableInfo> result = developerService.variableTypes(null, java.util.Set.of("string"), null, null);
        
        // All results should have non-null key (only processes, not cases)
        assertThat(result)
                .as("All results should have non-null process definition key")
                .extracting(VariableInfo::key)
                .doesNotContainNull();
    }

    /**
     * Tests that deadLetterJobs returns only process-related data.
     */
    @Test
    public void deadLetterJobsReturnsOnlyProcesses() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        List<DeadLetterJob> result = developerService.deadLetterJobs(null, null, null);

        // All results should have non-null key and procDefId (only processes)
        assertThat(result)
                .as("All results should have non-null process definition key and procDefId")
                .extracting(DeadLetterJob::key_, DeadLetterJob::procDefId)
                .doesNotContain(tuple(null, null));
    }

    /**
     * Tests that failingRuntimeJobs returns only process-related data.
     */
    @Test
    public void failingRuntimeJobsReturnsOnlyProcesses() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R100/PT5M")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                5_000, 500L,
                () -> managementService.createTimerJobQuery()
                        .processInstanceId(processInstance.getId())
                        .withException()
                        .count() > 0);

        List<FailingRuntimeJob> result = developerService.failingRuntimeJobs(null, null, null);

        // All results should have non-null key and procDefId (only processes)
        assertThat(result)
                .as("All results should have non-null process definition key and procDefId")
                .extracting(FailingRuntimeJob::key_, FailingRuntimeJob::procDefId)
                .doesNotContain(tuple(null, null));
    }

    /**
     * Tests that deadLetterJobDetails returns only process-related data.
     */
    @Test
    public void deadLetterJobDetailsReturnsOnlyProcesses() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();

        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                10_000, 500L,
                () -> managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).count() > 0);

        List<DeadLetterJobDetail> result = developerService.deadLetterJobDetails(null, null, null, null);

        // All results should have non-null key and procDefId (only processes)
        assertThat(result)
                .as("All results should have non-null process definition key and procDefId")
                .extracting(DeadLetterJobDetail::key_, DeadLetterJobDetail::procDefId)
                .doesNotContain(tuple(null, null));
    }

    /**
     * Tests that longRunningTransaction returns only process-related data.
     */
    @Test
    public void longRunningTransactionReturnsOnlyProcesses() {
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("longRunningLoop")
                .variable("threshold", 10)
                .start();

        List<LongRunningTransaction> result = developerService.longRunningTransaction(null, null);

        // All results should have non-null key and procDefId (only processes)
        assertThat(result)
                .as("All results should have non-null process definition key and procDefId")
                .extracting(LongRunningTransaction::key, LongRunningTransaction::procDefId)
                .doesNotContain(tuple(null, null));
    }
}
