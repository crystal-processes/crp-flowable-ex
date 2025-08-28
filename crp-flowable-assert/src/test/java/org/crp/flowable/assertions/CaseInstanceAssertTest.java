package org.crp.flowable.assertions;

import org.assertj.core.groups.Tuple;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.MilestoneInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.api.runtime.UserEventListenerInstance;
import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.engine.test.FlowableCmmnTest;
import org.flowable.cmmn.engine.test.impl.CmmnJobTestHelper;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.crp.flowable.assertions.TestUtils.createOneHumanTaskCase;

@FlowableCmmnTest
class CaseInstanceAssertTest {

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void isRunning(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).isRunning();
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void isRunningForNonRunningProcess(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).isRunning())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> to be running but is not.");
    }

    @Test
    void isRunningForNull() {
        assertThatThrownBy(() -> assertThat((CaseInstance) null).isRunning())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void hasVariable(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "variableName", "variableValue");

        assertThat(oneHumanTaskCase).hasVariable("variableName");
        assertThat(oneHumanTaskCase).hasVariable("variableName").isRunning();
        assertThat(oneHumanTaskCase).isRunning().hasVariable("variableName");
        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).hasVariable("nonExistingVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> has variable <nonExistingVariable> but variable does not exist.");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void hasVariableForNonRunningProcess(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "variableName", "variableValue");
        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).hasVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> has variable <variableName> but variable does not exist.");
        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).hasVariable("nonExistingVariable"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> has variable <nonExistingVariable> but variable does not exist.");
    }

    @Test
    void hasVariableForNull() {
        assertThatThrownBy(() -> assertThat((CaseInstance) null).hasVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void doesNotHaveVariable(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "variableName", "variableValue");

        assertThat(oneHumanTaskCase).doesNotHaveVariable("NonExistingVariableName");
        assertThat(oneHumanTaskCase).doesNotHaveVariable("NonExistingVariableName").isRunning();
        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).doesNotHaveVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> does not have variable <variableName> but variable exists.");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void doesNotHaveVariableForNonRunningProcess(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "variableName", "variableValue");
        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThat(oneHumanTaskCase).doesNotHaveVariable("variableName");
        assertThat(oneHumanTaskCase).doesNotHaveVariable("nonExistingVariable");
    }

    @Test
    void doesNotHaveVariableForNull() {
        assertThatThrownBy(() -> assertThat((CaseInstance) null).doesNotHaveVariable("variableName"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void doesNotExistForRunningInstance(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected case instance <oneHumanTaskCase, "+oneHumanTaskCase.getId()+"> is finished but instance exists in runtime.");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void doesNotExistForNonRunningProcess(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThat(oneHumanTaskCase).doesNotExist();
    }

    @Test
    void doesNotExistForNull() {
        assertThatThrownBy(() -> assertThat((CaseInstance) null).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void milestonesForRunningInstance(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).milestones().extracting(MilestoneInstance::getElementId)
                .contains("planItem2");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void milestonesForNonRunningProcess(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);
        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThat(oneHumanTaskCase).planItems().isEmpty();
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void planItems(CmmnRuntimeService cmmnRuntimeService, CmmnTaskService taskService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).as("Running case has at least 2 active planItems." +
                        "(CaseInstance + Child)")
                .planItems().extracting(PlanItemInstance::getCaseInstanceId).contains(oneHumanTaskCase.getId())
                        .hasSize(1);

        taskService.complete(taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult().getId());

        assertThat(oneHumanTaskCase).doesNotExist().as("There must be no execution for the finished case.")
                .planItems().hasSize(0);
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void variables(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).variables().isEmpty();
        assertThatThrownBy(() -> assertThat(oneHumanTaskCase).hasVariableWithValue("nonExistingVar", "anyValue"))
                .isInstanceOf(AssertionError.class).hasMessage("Expected case instance <oneHumanTaskCase, "
                        +oneHumanTaskCase.getId()+"> has variable <nonExistingVar> but variable does not exist.");

        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "testVariable", "initialValue");

        assertThat(oneHumanTaskCase).variables().extracting("name", "value").contains(Tuple.tuple("testVariable", "initialValue"));
        assertThat(oneHumanTaskCase).hasVariableWithValue("testVariable", "initialValue");

        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "testVariable", "updatedValue");

        assertThat(oneHumanTaskCase).variables().extracting("name", "value").contains(Tuple.tuple("testVariable", "updatedValue"));
        assertThat(oneHumanTaskCase).hasVariableWithValue("testVariable", "updatedValue").isRunning();

        cmmnRuntimeService.setVariable(oneHumanTaskCase.getId(), "firstVariable", "initialValue");
        assertThat(oneHumanTaskCase).as("Variables are ordered by names ascending.")
                .variables().extracting("name")
                .containsExactly("firstVariable", "testVariable");

    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void identityLinks(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = createOneHumanTaskCase(cmmnRuntimeService);

        assertThat(oneHumanTaskCase).identityLinks().isEmpty();
        assertThat(oneHumanTaskCase).inHistory().identityLinks().isEmpty();

        cmmnRuntimeService.addUserIdentityLink(oneHumanTaskCase.getId(), "testUser", IdentityLinkType.ASSIGNEE);
        cmmnRuntimeService.addGroupIdentityLink(oneHumanTaskCase.getId(), "testGroup", IdentityLinkType.CANDIDATE);

        assertThat(oneHumanTaskCase).identityLinks().hasSize(2)
                .extracting(IdentityLink::getUserId, IdentityLink::getGroupId, IdentityLink::getType)
                .containsExactlyInAnyOrder(Tuple.tuple("testUser", null, IdentityLinkType.ASSIGNEE),
                        Tuple.tuple(null, "testGroup", IdentityLinkType.CANDIDATE));
        assertThat(oneHumanTaskCase).inHistory().identityLinks().hasSize(2)
                .extracting(HistoricIdentityLink::getUserId, HistoricIdentityLink::getGroupId, HistoricIdentityLink::getType)
                .containsExactlyInAnyOrder(Tuple.tuple("testUser", null, IdentityLinkType.ASSIGNEE),
                        Tuple.tuple(null, "testGroup", IdentityLinkType.CANDIDATE));

    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void userTasks(CmmnRuntimeService cmmnRuntimeService,
                   CmmnTaskService taskService, CmmnEngine caseEngine) {
        CaseInstance oneHumanTaskCase = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("oneHumanTaskCase").startAsync();

        assertThat(oneHumanTaskCase).as("Process instance is started asynchronously and did not reach userTask")
                .userTasks().isEmpty();
        assertThat(oneHumanTaskCase).as("Process instance is started asynchronously and did not reach userTask in history")
                .inHistory().userTasks().isEmpty();

        CmmnJobTestHelper.waitForJobExecutorToProcessAllJobs(caseEngine, 10_000, 500, true);

        assertThat(oneHumanTaskCase).as("Async executions executed and userTask reached.")
                .userTasks().hasSize(1).extracting(Task::getTaskDefinitionKey).containsExactly("theTask");
        assertThat(oneHumanTaskCase).as("Async executions executed and userTask reached in history.")
                .inHistory().userTasks().hasSize(1).extracting(HistoricTaskInstance::getTaskDefinitionKey)
                .containsExactly("theTask");

        Task task = taskService.createTaskQuery().caseInstanceId(oneHumanTaskCase.getId()).singleResult();
        taskService.complete(task.getId());

        assertThat(oneHumanTaskCase).as("User tasks are empty for non existing case").doesNotExist()
                .userTasks().isEmpty();
        assertThat(oneHumanTaskCase).as("The userTask is completed, but must exist in history.")
                .inHistory().isFinished()
                .userTasks().hasSize(1).extracting(HistoricTaskInstance::getTaskDefinitionKey)
                .containsExactly("theTask");
    }

    @Test
    @CmmnDeployment(resources = "oneHumanTaskCase.cmmn")
    void subscriptionsWithoutSubscription(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCase = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("oneHumanTaskCase").start();

        assertThat(oneHumanTaskCase).as("One task case does not have any subscription")
                .userEventListeners().isEmpty();

    }

    @Test
    @CmmnDeployment(resources = "oneTaskCaseWithEvent.cmmn")
    void subscriptions(CmmnRuntimeService cmmnRuntimeService) {
        CaseInstance oneHumanTaskCaseWithSubscription = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("oneTaskCaseWithEvent").start();

        assertThat(oneHumanTaskCaseWithSubscription).as("One task case with subscription has exactly one subscription")
                .userEventListeners().hasSize(1).extracting(UserEventListenerInstance::getName).contains("eventMessage");
    }

    @Test
    void activitiesForNull() {
        assertThatThrownBy(() -> assertThat((CaseInstance) null).doesNotExist())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be null");
    }

}