package org.crp.flowable.assertions;

import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.cmmn.engine.CmmnEngines;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;

public class Utils {

    protected static String getProcessDescription(ProcessInstance actual) {
        return getProcessDescription(actual.getProcessDefinitionKey(), actual.getId());
    }

    protected static String getProcessDescription(HistoricProcessInstance actual) {
        return getProcessDescription(actual.getProcessDefinitionKey(), actual.getId());
    }

    protected static String getProcessDescription(String processDefinitionKey, String id) {
        return "Expected process instance <"+processDefinitionKey+", "+id+">";
    }

    protected static String getCaseDescription(CaseInstance actual) {
        return getCaseDescription(actual.getCaseDefinitionKey(), actual.getId());
    }

    protected static String getCaseDescription(HistoricCaseInstance actual) {
        return getCaseDescription(actual.getCaseDefinitionKey(), actual.getId());
    }

    protected static String getCaseDescription(String caseDefinitionKey, String id) {
        return "Expected case instance <"+caseDefinitionKey+", "+id+">";
    }

    protected static TaskService getTaskService() {
        return getProcessEngine().getTaskService();
    }

    protected static RuntimeService getRuntimeService() {
        return getProcessEngine().getRuntimeService();
    }

    protected static HistoryService getHistoryService() {
        return getProcessEngine().getHistoryService();
    }

    protected static ProcessEngine getProcessEngine() {
        return ProcessEngines.getProcessEngines().get("default");
    }

    protected static CmmnTaskService getCmmnTaskService() {
        return getCmmnEngine().getCmmnTaskService();
    }

    protected static CmmnRuntimeService getCmmnRuntimeService() {
        return getCmmnEngine().getCmmnRuntimeService();
    }

    protected static CmmnHistoryService getCmmnHistoryService() {
        return getCmmnEngine().getCmmnHistoryService();
    }

    protected static CmmnEngine getCmmnEngine() {
        return CmmnEngines.getCmmnEngines().get("default");
    }

}
