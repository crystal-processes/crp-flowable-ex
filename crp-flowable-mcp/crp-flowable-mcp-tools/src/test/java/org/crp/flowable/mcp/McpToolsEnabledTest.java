package org.crp.flowable.mcp;

import org.crp.flowable.mcp.service.DeveloperService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class McpToolsEnabledTest {
    @Nested
    @TestPropertySource(properties = "crp.flowable.mcp.enabled=false")
    class WhenEnabledIsFalse {
        @Test
        void developerService_shouldNotExist(ApplicationContext context) {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(DeveloperService.class));
        }
    }

    @Nested
    class WhenEnabledNotSet {
        @Test
        void developerService_shouldNotExist(ApplicationContext context) {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(DeveloperService.class));
        }
    }
}
