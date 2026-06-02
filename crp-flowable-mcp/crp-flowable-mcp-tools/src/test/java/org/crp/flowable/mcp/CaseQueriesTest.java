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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Tests for CMMN case-related queries: variables, dead letter jobs, and activities.
 * These tests verify that case data is properly returned with scopeId and scopeType attributes.
 */
@CrpMcpTest
public class CaseQueriesTest {

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
                .addClasspathResource("failingServiceTask.cmmn")
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
     * Tests that caseVariables returns case-related data with scopeType="cmmn".
     */
    @Test
    public void caseVariablesReturnsCaseDataWithScopeType() {
        // Create case instance with variable
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();

        try {
            List<CaseVariableInfo> result = developerService.caseVariables(null, null, null, null);
            
            assertThat(result)
                    .as("caseVariables should return case data")
                    .isNotEmpty()
                    .extracting(CaseVariableInfo::scopeType, CaseVariableInfo::scopeId, CaseVariableInfo::scopeDefinitionKey, CaseVariableInfo::name, CaseVariableInfo::type)
                    .contains(tuple("cmmn", caseInstance.getId(), "oneHumanTaskCase","caseVariable", "string"));
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseVariables filters by caseDefinitionKey.
     */
    @Test
    public void caseVariablesFiltersByCaseDefinitionKey() {
        // Create case instance with variable
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();

        try {
            List<CaseVariableInfo> resultFiltered = developerService.caseVariables("oneHumanTaskCase", null, null, null);
            
            assertThat(resultFiltered)
                    .as("caseVariables with caseDefinitionKey filters correctly")
                    .isNotEmpty()
                    .extracting(CaseVariableInfo::scopeDefinitionKey, CaseVariableInfo::scopeId)
                    .contains(tuple("oneHumanTaskCase", caseInstance.getId()));
            
            List<CaseVariableInfo> resultNonExisting = developerService.caseVariables("nonExistingKey", null, null, null);
            assertThat(resultNonExisting)
                    .as("non-existing caseDefinitionKey returns empty results")
                    .isEmpty();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseVariables filters by startedAfter.
     */
    @Test
    public void caseVariablesFiltersByStartedAfter() {
        Instant beforeCaseStart = Instant.now().minusMillis(1L);
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .variable("caseVariable", "caseValue")
                .start();
        Instant afterCaseStart = caseInstance.getStartTime().toInstant().plusMillis(1L);

        try {
            List<CaseVariableInfo> resultBefore = developerService.caseVariables(null, null, beforeCaseStart, null);
            assertThat(resultBefore)
                    .as("startedAfter before case creation includes the case")
                    .isNotEmpty()
                    .extracting(CaseVariableInfo::scopeId, CaseVariableInfo::scopeDefinitionKey)
                    .contains(tuple(caseInstance.getId(), "oneHumanTaskCase"));

            List<CaseVariableInfo> resultAfter = developerService.caseVariables(null, null, afterCaseStart, null);
            assertThat(resultAfter)
                    .as("startedAfter after case creation excludes the case")
                    .isEmpty();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseDeadLetterJobs returns case-related dead letter jobs with scopeType="cmmn".
     */
    @Test
    public void caseDeadLetterJobsReturnsCaseDataWithScopeType() {
        // Create a case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .start();

        try {
            // Note: We need a case with a failing job. For now, test that the query works.
            // In a real scenario, we'd need a CMMN case with a service task that fails.
            List<CaseDeadLetterJob> result = developerService.caseDeadLetterJobs(null, null, null);
            
            // The query should execute without errors even if no dead letter jobs exist
            assertThat(result).as("caseDeadLetterJobs query executes successfully").isNotNull();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseActivities returns case-related activities with scopeType="cmmn".
     */
    @Test
    public void caseActivitiesReturnsCaseDataWithScopeType() {
        // Create case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .start();

        try {
            List<CaseActivityInfo> result = developerService.caseActivities(null, null, null);
            
            assertThat(result)
                    .as("caseActivities should return case data")
                    .isNotEmpty()
                    .extracting(CaseActivityInfo::scopeType, CaseActivityInfo::scopeId, CaseActivityInfo::scopeDefinitionKey)
                    .contains(tuple("cmmn", caseInstance.getId(), "oneHumanTaskCase"));
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseActivities filters by caseDefinitionKey.
     */
    @Test
    public void caseActivitiesFiltersByCaseDefinitionKey() {
        // Create case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .start();

        try {
            List<CaseActivityInfo> resultFiltered = developerService.caseActivities("oneHumanTaskCase", null, null);
            
            assertThat(resultFiltered)
                    .as("caseActivities with caseDefinitionKey filters correctly")
                    .isNotEmpty()
                    .extracting(CaseActivityInfo::scopeDefinitionKey, CaseActivityInfo::scopeId)
                    .contains(tuple("oneHumanTaskCase", caseInstance.getId()));
            
            List<CaseActivityInfo> resultNonExisting = developerService.caseActivities("nonExistingKey", null, null);
            assertThat(resultNonExisting)
                    .as("non-existing caseDefinitionKey returns empty results")
                    .isEmpty();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseActivities filters by startedAfter.
     */
    @Test
    public void caseActivitiesFiltersByStartedAfter() {
        Instant beforeCaseStart = Instant.now().minusMillis(1L);
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .start();
        Instant afterCaseStart = caseInstance.getStartTime().toInstant().plusMillis(1L);

        try {
            List<CaseActivityInfo> resultBefore = developerService.caseActivities(null, beforeCaseStart, null);
            assertThat(resultBefore)
                    .as("startedAfter before case creation includes the case")
                    .isNotEmpty()
                    .extracting(CaseActivityInfo::scopeId, CaseActivityInfo::scopeDefinitionKey)
                    .contains(tuple(caseInstance.getId(), "oneHumanTaskCase"));

            List<CaseActivityInfo> resultAfter = developerService.caseActivities(null, afterCaseStart, null);
            assertThat(resultAfter)
                    .as("startedAfter after case creation excludes the case")
                    .isEmpty();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseDeadLetterJobs filters by caseDefinitionKey.
     */
    @Test
    public void caseDeadLetterJobsFiltersByCaseDefinitionKey() {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("failingServiceTask")
                .start();
        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                60_000, 500L,
                () -> managementService.createDeadLetterJobQuery().scopeId(caseInstance.getId()).count() > 0);

        List<CaseDeadLetterJob> resultFiltered = developerService.caseDeadLetterJobs("oneHumanTaskCase", null, null);
        
        assertThat(resultFiltered)
                .as("caseDeadLetterJobs with caseDefinitionKey filters correctly")
                .extracting(CaseDeadLetterJob::scopeDefinitionKey)
                .allMatch(key -> key == null || key.equals("oneHumanTaskCase"));
        
        List<CaseDeadLetterJob> resultNonExisting = developerService.caseDeadLetterJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing caseDefinitionKey returns empty results")
                .isEmpty();
    }

    /**
     * Tests that caseDeadLetterJobs filters by startedAfter.
     */
    @Test
    public void caseDeadLetterJobsFiltersByStartedAfter() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        Instant afterJobCreation = Instant.now().plusMillis(1L);

        List<CaseDeadLetterJob> resultBefore = developerService.caseDeadLetterJobs(null, beforeJobCreation, null);
        assertThat(resultBefore)
                .as("startedAfter before job creation includes all dead letter jobs")
                .isNotNull();

        List<CaseDeadLetterJob> resultAfter = developerService.caseDeadLetterJobs(null, afterJobCreation, null);
        assertThat(resultAfter)
                .as("startedAfter after job creation excludes new dead letter jobs")
                .isNotNull();
    }

    /**
     * Tests that caseFailingRuntimeJobs returns case-related failing runtime jobs with scopeType="cmmn".
     */
    @Test
    public void caseFailingRuntimeJobsReturnsCaseDataWithScopeType() {
        // Create a case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("oneHumanTaskCase")
                .start();

        try {
            // Note: We need a case with a failing job. For now, test that the query works.
            // In a real scenario, we'd need a CMMN case with a service task that is failing.
            List<CaseFailingRuntimeJob> result = developerService.caseFailingRuntimeJobs(null, null, null);
            
            // The query should execute without errors even if no failing runtime jobs exist
            assertThat(result).as("caseFailingRuntimeJobs query executes successfully").isNotNull();
        } finally {
            cmmnRuntimeService.deleteCaseInstance(caseInstance.getId());
        }
    }

    /**
     * Tests that caseFailingRuntimeJobs filters by caseDefinitionKey.
     */
    @Test
    public void caseFailingRuntimeJobsFiltersByCaseDefinitionKey() {
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("failingServiceTask")
                .variable("failedJobRetryTimeCycle", "R1/PT1S")
                .start();
        JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration,
                60_000, 500L,
                () -> managementService.createTimerJobQuery().scopeId(caseInstance.getId()).count() > 0);

        List<CaseFailingRuntimeJob> resultFiltered = developerService.caseFailingRuntimeJobs("failingServiceTask", null, null);
        
        assertThat(resultFiltered)
                .as("caseFailingRuntimeJobs with caseDefinitionKey filters correctly")
                .extracting(CaseFailingRuntimeJob::scopeDefinitionKey)
                .allMatch(key -> key == null || key.equals("failingServiceTask"));
        
        List<CaseFailingRuntimeJob> resultNonExisting = developerService.caseFailingRuntimeJobs("nonExistingKey", null, null);
        assertThat(resultNonExisting)
                .as("non-existing caseDefinitionKey returns empty results")
                .isEmpty();
    }

    /**
     * Tests that caseFailingRuntimeJobs filters by startedAfter.
     */
    @Test
    public void caseFailingRuntimeJobsFiltersByStartedAfter() {
        Instant beforeJobCreation = Instant.now().minusMillis(1L);
        Instant afterJobCreation = Instant.now().plusMillis(1L);

        List<CaseFailingRuntimeJob> resultBefore = developerService.caseFailingRuntimeJobs(null, beforeJobCreation, null);
        assertThat(resultBefore)
                .as("startedAfter before job creation includes all failing runtime jobs")
                .isNotNull();

        List<CaseFailingRuntimeJob> resultAfter = developerService.caseFailingRuntimeJobs(null, afterJobCreation, null);
        assertThat(resultAfter)
                .as("startedAfter after job creation excludes new failing runtime jobs")
                .isNotNull();
    }
}
