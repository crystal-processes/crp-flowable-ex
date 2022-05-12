package org.crp.flowable.shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest(properties = { InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=" + false })
public class RawRestIT {
    @Autowired
    private Shell shell;

    @Test
    void deployModel() throws IOException {
        // given: deploy app
        assertThat(shell.evaluate(() -> "deploy src/test/resources/app.bar --deployment-name testFileName.bar").toString()).
                contains("\"name\":\"testFileName\"");


        // get
        ObjectNode deploymentsQueryResult = (ObjectNode) shell.evaluate(() -> "ex GET /app-api/app-repository/deployments");
        assertThat(deploymentsQueryResult.toString()).
                contains("\"name\":\"testFileName\"");
        String deploymentId = deploymentsQueryResult.get("data").get(0).get("id").asText();

        // post
        ObjectNode startProcessResult = (ObjectNode) shell.evaluate(
                () -> "ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}"
        );
        String processId = startProcessResult.get("id").asText();
        assertThat(processId).isNotEmpty();

        // put
        assertThat(shell.evaluate(() -> "exl PUT /process-api/runtime/process-instances/"+processId+" {\"name\":\"nameone\"}").toString()).contains("nameone");

        // delete
        assertThat(shell.evaluate(() -> "ex DELETE /app-api/app-repository/deployments/"+deploymentId).toString()).isEqualTo("{}");

    }

}
