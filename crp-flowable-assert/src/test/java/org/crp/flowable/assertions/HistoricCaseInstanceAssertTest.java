package org.crp.flowable.assertions;

import org.assertj.core.groups.Tuple;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricMilestoneInstance;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.engine.test.FlowableCmmnTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.TestUtils.createOneHumanTaskCase;

@FlowableCmmnTest
class HistoricCaseInstanceAssertTest {

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void isFinishedForFinishedCaseInstance(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService cmmnTaskService, CmmnHistoryService historyService) {
        CaseInstance oneTaskProcess = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneTaskProcess).inHistory().milestones().extracting(HistoricMilestoneInstance::getElementId).contains(
                        "planItem2"
                );

        cmmnTaskService.complete(cmmnTaskService.createTaskQuery().caseInstanceId(oneTaskProcess.getId()).singleResult().getId());

        assertThat(oneTaskProcess).inHistory().isFinished()
            .milestones().extracting(HistoricMilestoneInstance::getElementId).contains(
                "planItem2"
            );

        HistoricCaseInstance historicCaseInstance = historyService.createHistoricCaseInstanceQuery().caseInstanceId(oneTaskProcess.getId()).singleResult();
        assertThat(historicCaseInstance).isFinished()
                .milestones().extracting(HistoricMilestoneInstance::getElementId).contains(
                        "planItem2"
                );
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void variables(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService cmmnTaskService) {
        CaseInstance oneTaskProcess = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneTaskProcess).as("No variable exists in the case scope.")
                .inHistory().variables().isEmpty();
        
        cmmnRuntimeService.setVariable(oneTaskProcess.getId(), "testVariable", "variableValue");

        assertThat(oneTaskProcess).as("Variable exists in the case scope, the variable must be present in the history.")
                .inHistory()
                .hasVariable("testVariable")
                .hasVariableWithValue("testVariable", "variableValue")
                .variables().hasSize(1).extracting("name", "value").
                containsExactly(Tuple.tuple("testVariable", "variableValue"));

        Task task = cmmnTaskService.createTaskQuery().caseInstanceId(oneTaskProcess.getId()).singleResult();
        cmmnTaskService.complete(task.getId());

        assertThat(oneTaskProcess).as("Variable exists in the case scope, the variable must be present in the history.")
                .doesNotExist()
                .inHistory()
                .isFinished()
                .hasVariable("testVariable")
                .hasVariableWithValue("testVariable", "variableValue")
                .variables().hasSize(1).extracting("name", "value").
                containsExactly(Tuple.tuple("testVariable", "variableValue"));
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void hasVariable(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneTaskProcess = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneTaskProcess).as("No variable exists in the case scope.")
                .inHistory().variables().isEmpty();

        cmmnRuntimeService.setVariable(oneTaskProcess.getId(), "testVariable", "variableValue");

        assertThat(oneTaskProcess).as("Variable exists in the case scope, the variable must be present in the history.")
                .inHistory().variables().hasSize(1).extracting("name", "value").
                containsExactly(Tuple.tuple("testVariable", "variableValue"));
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void doesNotHaveVariable(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).as("No variable exists in the case scope.")
                .inHistory().doesNotHaveVariable("nonExistingVariable");

        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "testVariable", "variableValue");

        assertThat(oneHumanTaskCase).as("Variable exists in the case scope, the variable must be present in the history.")
                .inHistory().doesNotHaveVariable("nonExistingVariable")
                .hasVariable("testVariable");

        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).inHistory().doesNotHaveVariable("testVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> does not have variable <testVariable> but variable exists in history.");
    }

}