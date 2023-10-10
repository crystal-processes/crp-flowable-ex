package org.crp.flowable.bpmn;

import org.apache.commons.io.IOUtils;
import org.crp.flowable.coverage.bpmn.ReportFlowableEngineAgendaFactory;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.test.Deployment;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.impl.test.FlowableSpringExtension;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(FlowableSpringExtension.class)
@ActiveProfiles("test")
public class SpringBootGenerateEventsTest {

    public static final String EVENT_REPORT_FILE_NAME = "target/bpmn-coverage-report.csv";

    @AfterEach
    void deleteGeneratedEventsFile() {
        File file = new File(EVENT_REPORT_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @Deployment(resources = "org/crp/flowable/test/oneTaskProcess.bpmn20.xml")
    void oneTaskProcessReport(RuntimeService runtimeService, TaskService taskService) throws IOException {
        // when
        String processId = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTaskProcess").start().getId();
        Task task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        taskService.complete(task.getId());

        // then
        try (BufferedReader brGenerated = new BufferedReader(new FileReader(EVENT_REPORT_FILE_NAME))) {
            try (BufferedReader brExpected = new BufferedReader(new FileReader("src/test/resources/eventReport.txt"))) {
                assertThat(IOUtils.contentEquals(brGenerated, brExpected));
            }
        }
    }

    @TestConfiguration
    static class AddReportAgendaFactory {
        @Bean
        public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> addReportAgendaFactoryConfigurationConfigurer(
                ReportFlowableEngineAgendaFactory reportFlowableEngineAgendaFactory
        ) {
            return processEngineConfiguration -> processEngineConfiguration.setAgendaFactory(reportFlowableEngineAgendaFactory);
        }

        @Bean
        public ReportFlowableEngineAgendaFactory reportAgendaFactory() {
            return new ReportFlowableEngineAgendaFactory(EVENT_REPORT_FILE_NAME);
        }
    }
}
