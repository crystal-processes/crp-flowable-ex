package org.crp.flowable.form;

import org.crp.flowable.form.impl.FormEngineConfigurator;
import org.crp.flowable.form.impl.RawFormHandlerInterceptor;
import org.crp.flowable.form.impl.ResourceFormService;
import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.rest.service.api.FormHandlerRestApiInterceptor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FormEngineConfiguration {

    @Bean
    FormHandlerRestApiInterceptor sampleFormHandlerInterceptor() {
        return new RawFormHandlerInterceptor();
    }

    @Bean
    org.crp.flowable.form.impl.FormEngineConfiguration formEngineConfiguration() {
        return new org.crp.flowable.form.impl.FormEngineConfiguration().setFormService(new ResourceFormService());
    }

    @Bean
    FormEngineConfigurator sampleFormEngineConfigurator(org.crp.flowable.form.impl.FormEngineConfiguration formEngineConfiguration) {
        return new FormEngineConfigurator().setFormEngineConfiguration(formEngineConfiguration);
    }

    @Bean
    @ConditionalOnClass(SpringAppEngineConfiguration.class)
    EngineConfigurationConfigurer<SpringAppEngineConfiguration> addFormEngineToAppEngine(FormEngineConfigurator formEngineConfigurator) {
        return appEngineConfiguration -> appEngineConfiguration.addConfigurator(formEngineConfigurator);
    }

    @Bean
    @ConditionalOnClass(SpringProcessEngineConfiguration.class)
    EngineConfigurationConfigurer<SpringProcessEngineConfiguration> addFormEngineToProcessEngine(FormEngineConfigurator formEngineConfigurator) {
        return configuration -> configuration.addConfigurator(formEngineConfigurator);
    }
}