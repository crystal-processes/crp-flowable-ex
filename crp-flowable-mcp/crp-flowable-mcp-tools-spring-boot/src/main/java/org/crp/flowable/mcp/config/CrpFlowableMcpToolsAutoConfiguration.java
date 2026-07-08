package org.crp.flowable.mcp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(CrpFlowableMcpToolsConfiguration.class)
@ConditionalOnProperty(name = "crp.flowable.mcp.enabled", havingValue = "true")
@EnableConfigurationProperties({
        CrpFlowableMcpProperties.class
})
public class CrpFlowableMcpToolsAutoConfiguration {
}
