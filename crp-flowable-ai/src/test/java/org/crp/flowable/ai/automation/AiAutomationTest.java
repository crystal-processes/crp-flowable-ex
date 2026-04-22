package org.crp.flowable.ai.automation;

import org.flowable.engine.test.ConfigurationResource;
import org.flowable.engine.test.FlowableTest;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@FlowableTest
@ConfigurationResource("flowable-ai.cfg.xml")
@ExtendWith(AiAutomationExtension.class)
public @interface AiAutomationTest {
}
