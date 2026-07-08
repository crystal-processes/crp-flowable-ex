package org.crp.flowable.mcp;

import org.flowable.cmmn.spring.impl.test.FlowableCmmnSpringExtension;
import org.flowable.spring.impl.test.FlowableSpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = { org.crp.flowable.mcp.config.TestConfiguration.class })
@ExtendWith(FlowableCmmnSpringExtension.class)
@ExtendWith(FlowableSpringExtension.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public @interface CrpMcpTest {
}
