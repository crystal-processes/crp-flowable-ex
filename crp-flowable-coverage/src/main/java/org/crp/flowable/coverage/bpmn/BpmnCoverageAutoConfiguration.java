package org.crp.flowable.coverage.bpmn;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration
@AutoConfigureAfter(ProcessEngineAutoConfiguration.class)
@EnableConfigurationProperties(BpmnCoverageProperties.class)
@ConditionalOnClass(SpringProcessEngineConfiguration.class)
@ConditionalOnProperty(prefix = "crp.flowable", name = "coverage.enabled", havingValue = "true")
public class BpmnCoverageAutoConfiguration {

    @Bean
    EngineConfigurationConfigurer<SpringProcessEngineConfiguration> setReportFlowableEngineAgendaFactory(BpmnCoverageProperties properties) {
        return processEngineConfiguration -> processEngineConfiguration.setAgendaFactory(new ReportFlowableEngineAgendaFactory(properties.getReportPath()));
    }

}
