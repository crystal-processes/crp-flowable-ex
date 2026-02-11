package org.crp.flowable.assertions;

import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;

public abstract class TestUtils {
    static ProcessInstance createOneTaskProcess(RuntimeService runtimeService) {
        return runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTaskProcess").start();
    }
    static CaseInstance createOneHumanTaskCase(CmmnRuntimeService cmmnRuntimeService) {
        return cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("oneHumanTaskCase").start();
    }
}
