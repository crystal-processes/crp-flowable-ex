package org.crp.flowable.shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest(properties = { InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=" + false })
public class TemplateProcessorIT {
    @Autowired
    private Shell shell;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateJUnitTest() throws IOException {
        JsonNode deployment = createDeployment();

        String processInstanceId = generateHistory();

        shell.evaluate(() -> "gt "+ processInstanceId +" OneTaskProcessTest --sourceDir target/");

        assertThat(IOUtils.contentEquals(getReader("src/test/resources/jUnitTest.java"), getReader("target/OneTaskProcessTest.java")));

        // cleanup
        (new File("target/OneTaskProcessTest.java")).delete();
        assertThat(shell.evaluate(() -> "exl DELETE /app-api/app-repository/deployments/"+deployment.get("id").asText()).toString()).isEqualTo("{}");

    }

    private JsonNode createDeployment() {
        JsonNode deployment = (JsonNode) shell.evaluate(() -> "deploy src/test/resources/app.bar --deployment-name testFileName.bar");
        assertThat(deployment.toString()).
                contains("\"name\":\"testFileName\"");
        return deployment;
    }

    private Reader getReader(String s) throws FileNotFoundException {
        return new BufferedReader(new FileReader(s));
    }

    private String generateHistory() {
        // start process
        ObjectNode startProcessResult = (ObjectNode) shell.evaluate(
                () -> "ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}"
        );
        String processId = startProcessResult.get("id").asText();
        assertThat(processId).isNotEmpty();
        ObjectNode tasks = (ObjectNode) shell.evaluate(() -> "exl POST /app/rest/query/tasks {\"processInstanceId\":\""+processId+"\"}");
        String taskId = tasks.get("data").get(0).get("id").asText();
        shell.evaluate(() -> "exl PUT /app/rest/tasks/"+taskId+"/action/complete");

        return processId;
    }
}
