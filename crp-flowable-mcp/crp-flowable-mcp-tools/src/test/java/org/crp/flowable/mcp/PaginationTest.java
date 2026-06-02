package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.crp.flowable.mcp.service.DeveloperService.VariableInfo;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to verify that pagination limits results to first 50 rows.
 * This test ensures that all queries return at most 50 results,
 * which is the configured RowBounds limit in DeveloperService.getSelectList().
 */
@CrpMcpTest
public class PaginationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private DeveloperService developerService;

    private Deployment deployment;

    @BeforeEach
    void deployProcess() {
        deployment = repositoryService.createDeployment()
                .addClasspathResource("oneTask.bpmn20.xml")
                .deploy();
    }

    @AfterEach
    void deleteDeployment() {
        if (deployment != null) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    /**
     * Tests that variableTypes returns at most 50 results even when more than 50 exist.
     * This verifies the pagination limit of RowBounds(0, 50) in getSelectList().
     */
    @Test
    public void variableTypesReturnsAtMost50Results() {
        // Create 55 process instances with unique variable names (more than the pagination limit)
        int totalInstances = 55;
        for (int i = 0; i < totalInstances; i++) {
            runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("oneTask")
                    .variable("testVariable" + i, "testValue" + i)
                    .start();
        }

        // Query for all variable types - should be limited to 50
        List<VariableInfo> result = developerService.variableTypes(null, null, null, null);

        assertThat(result)
                .as("Results should be limited to 50 rows by pagination")
                .hasSizeLessThanOrEqualTo(50);
    }

    /**
     * Tests that maxVariablesPerProcessDefinition returns at most 50 results.
     */
    @Test
    public void maxVariablesPerProcessDefinitionReturnsAtMost50Results() {
        // Create 55 process instances with variables
        int totalInstances = 55;
        for (int i = 0; i < totalInstances; i++) {
            runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("oneTask")
                    .variable("testVariable" + i, "testValue" + i)
                    .start();
        }

        List<DeveloperService.MaxVariableCount> result = 
                developerService.maxVariablesPerProcessDefinition(null, null, null);

        assertThat(result)
                .as("Results should be limited to 50 rows by pagination")
                .hasSizeLessThanOrEqualTo(50);
    }
}
