package org.crp.flowable.assertions;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;

public abstract class TestUtils {
    static ProcessInstance createOneTaskProcess(RuntimeService runtimeService) {
        return runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTaskProcess").start();
    }
}
