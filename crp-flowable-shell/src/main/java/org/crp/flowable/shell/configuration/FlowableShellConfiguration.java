package org.crp.flowable.shell.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableShellConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
