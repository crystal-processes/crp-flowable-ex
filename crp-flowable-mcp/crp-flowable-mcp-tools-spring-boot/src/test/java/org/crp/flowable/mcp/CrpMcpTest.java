package org.crp.flowable.mcp;

import org.crp.flowable.mcp.config.CrpFlowableMcpToolsAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(classes = CrpFlowableMcpToolsAutoConfiguration.class)
@ActiveProfiles("test")
public @interface CrpMcpTest {
}