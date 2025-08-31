package org.crp.flowable.ai.delegates;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat;
import static org.mockito.Mockito.*;

@FlowableTest
class AddToVectorStoreJavaDelegateTest {

    VectorStore vectorStore = mock(VectorStore.class);

    @AfterEach
    void resetMocks() {
        reset(vectorStore);
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/addToVectorStoreCallProcess.bpmn")
    void addDocToVectorStore(RuntimeService runtimeService) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("addToVectorStoreCallProcess")
                .transientVariables(Map.of("vectorStore", vectorStore,
                        "documentUrl", "org/crp/flowable/ai/delegates/addToVectorStoreCallProcess.bpmn")
                ).start();

        assertThat(processInstance).isRunning().activities().extracting(ActivityInstance::getActivityId).containsExactlyInAnyOrder(
                "startnoneevent1",
                "SequenceFlow_2",
                "addToVectorStoreTask",
                "SequenceFlow_4",
                "ReceiveTask_3"
        );
        verify(vectorStore, times(1)).add(anyList());
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/addToVectorStoreCallProcess.bpmn")
    void wrongDocUrl(RuntimeService runtimeService) {
        assertThatThrownBy(() -> runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("addToVectorStoreCallProcess")
                .transientVariables(Map.of("vectorStore", vectorStore,
                        "documentUrl", "nonExistingFile")
                ).start()
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nonExistingFile");
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/addToVectorStoreCallProcess.bpmn")
    void missingMandatoryVectorStore(RuntimeService runtimeService) {
        assertThatThrownBy(() -> runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("addToVectorStoreCallProcess")
                .transientVariables(Map.of(
                        "documentUrl", "")
                ).start()
        ).isInstanceOf(FlowableException.class)
                .hasMessageContaining("Unknown property used in expression: ${vectorStore}");
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    @Deployment(resources = "org/crp/flowable/ai/delegates/addToVectorStoreCallProcess.bpmn")
    void missingMandatoryDocUrl(RuntimeService runtimeService) {
        assertThatThrownBy(() -> runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("addToVectorStoreCallProcess")
                .transientVariable("vectorStore", vectorStore)
                .start()
        ).isInstanceOf(FlowableException.class)
                .hasMessageContaining("Unknown property used in expression: ${documentUrl}");
        verify(vectorStore, never()).add(anyList());
    }

}