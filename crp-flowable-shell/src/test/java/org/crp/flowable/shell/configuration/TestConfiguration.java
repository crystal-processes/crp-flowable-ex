package org.crp.flowable.shell.configuration;

import org.crp.flowable.shell.EvaluableShell;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Shell;

@Configuration
public class TestConfiguration {

    @Bean
    EvaluableShell evaluableShell(Shell shell) {
        return new EvaluableShell(shell);
    }
}
