package org.crp.flowable.assertions;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RuntimeService;
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

    protected static RuntimeService getRuntimeService() {
        return getProcessEngine().getRuntimeService();
    }

    protected static HistoryService getHistoryService() {
        return getProcessEngine().getHistoryService();
    }

    protected static ProcessEngine getProcessEngine() {
        return ProcessEngines.getProcessEngines().get("default");
    }

}
