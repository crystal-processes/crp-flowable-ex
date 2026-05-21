package org.crp.flowable.shell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellScreen;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateProcessorIT extends AbstractCommandTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateJUnitTest() throws Exception {
        FileUtils.delete(new File("target/OneTaskProcessTest.java"));
        JsonNode deployment = createDeployment();

        String processInstanceId = generateHistory();
        FileUtils.forceMkdir(new File("target"));

        execute("generate-test "+ processInstanceId +" OneTaskProcessTest --sourceDir target/");

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            assertTrue(IOUtils.contentEquals(getReader("src/test/resources/jUnitTest.java"), getReader("target/OneTaskProcessTest.java")))
        );


        // cleanup
        (new File("target/OneTaskProcessTest.java")).delete();

        execute("exl DELETE /app-api/app-repository/deployments/"+deployment.get("id"));
        assertScreenContainsText("{ }");

    }

    private JsonNode createDeployment() throws Exception {
        ShellScreen screen = client.sendCommand("deploy src/test/resources/app.bar --deployment-name testFileName.bar");
        String output = screen.toString();
        JsonNode deployment = objectMapper.readTree(output);
        assertThat(deployment.toString()).
                contains("\"name\":\"testFileName\"");
        return deployment;
    }

    private Reader getReader(String s) throws FileNotFoundException {
        return new BufferedReader(new FileReader(s));
    }

    private String generateHistory() throws Exception {
        // start process
        ShellScreen screen = client.sendCommand("ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}");
        ObjectNode startProcessResult = objectMapper.readValue(screen.toString(), ObjectNode.class);
        String processId = startProcessResult.get("id").asText();
        assertThat(processId).isNotEmpty();
        screen = client.sendCommand("exl POST /app/rest/query/tasks {\"processInstanceId\":\""+processId+"\"}");
        ObjectNode tasks = objectMapper.readValue(screen.toString(), ObjectNode.class);
        String taskId = tasks.get("data").get(0).get("id").asText();
        execute("exl PUT /app/rest/tasks/"+taskId+"/action/complete");

        return processId;
    }
}
