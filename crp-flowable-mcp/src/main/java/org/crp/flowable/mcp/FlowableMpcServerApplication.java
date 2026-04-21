package org.crp.flowable.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.crp.flowable.mcp.config.FlowableMcpProperties;

@SpringBootApplication
public class FlowableMpcServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowableMpcServerApplication.class, args);
    }
}
