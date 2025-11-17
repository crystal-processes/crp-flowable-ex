package org.crp.flowable.mpc;

import org.crp.flowable.mpc.service.FlowableService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class FlowableMcpServerConfiguration {

    @Bean
    public ToolCallbackProvider flowableTools(FlowableService service) {
        return MethodToolCallbackProvider.builder().toolObjects(service).build();
    }

}