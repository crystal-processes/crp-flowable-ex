package org.crp.flowable.mpc;

import org.crp.flowable.mpc.service.DeveloperService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootConfiguration
public class FlowableMcpServerConfiguration {

    @Bean
    public ToolCallbackProvider flowableTools(DeveloperService developerService) {
        return MethodToolCallbackProvider.builder().toolObjects(developerService).build();
    }

    @Bean
    DeveloperService developerService(JdbcTemplate jdbcTemplate){
        return new DeveloperService(jdbcTemplate);
    }

}