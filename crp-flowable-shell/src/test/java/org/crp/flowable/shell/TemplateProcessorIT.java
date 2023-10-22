package org.crp.flowable.shell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.crp.flowable.shell.commands.Deployment;
import org.crp.flowable.shell.commands.RawRest;
import org.crp.flowable.shell.commands.TemplateProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.test.context.ContextConfiguration;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {Deployment.class, RawRest.class, TemplateProcessor.class, FreeMarkerAutoConfiguration.class})
public class TemplateProcessorIT extends AbstractCommandTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateJUnitTest() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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

    private JsonNode createDeployment() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        JsonNode deployment = (JsonNode) evaluableShell.evaluate(() -> "deploy src/test/resources/app.bar --deployment-name testFileName.bar");
        assertThat(deployment.toString()).
                contains("\"name\":\"testFileName\"");
        return deployment;
    }

    private Reader getReader(String s) throws FileNotFoundException {
        return new BufferedReader(new FileReader(s));
    }

    private String generateHistory() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // start process
        ObjectNode startProcessResult = (ObjectNode) evaluableShell.evaluate(
                () -> "ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}"
        );
        String processId = startProcessResult.get("id").asText();
        assertThat(processId).isNotEmpty();
        ObjectNode tasks = (ObjectNode) evaluableShell.evaluate(() -> "exl POST /app/rest/query/tasks {\"processInstanceId\":\""+processId+"\"}");
        String taskId = tasks.get("data").get(0).get("id").asText();
        execute("exl PUT /app/rest/tasks/"+taskId+"/action/complete");

        return processId;
    }
}
