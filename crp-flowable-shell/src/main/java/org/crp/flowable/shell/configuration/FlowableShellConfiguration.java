package org.crp.flowable.shell.configuration;

import org.crp.flowable.shell.FlowableCliApplicationRunner;
import org.crp.flowable.shell.utils.JsonNodeResultHandler;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.jline.PromptProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class FlowableShellConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    ResultHandler<JsonNode> jsonNodeResultHandler() {
        return new JsonNodeResultHandler();
    }

    @Bean
    public PromptProvider flowableShellPromptProvider() {
        return () -> new AttributedString("flowable-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return new FlowableCliApplicationRunner();
    }

}
