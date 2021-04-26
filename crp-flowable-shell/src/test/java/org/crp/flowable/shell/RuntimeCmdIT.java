package org.crp.flowable.shell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = { InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=" + false })
class RuntimeCmdIT {
    @Autowired
    private Shell shell;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void deployApp() {
        assertThat(shell.evaluate(() -> "deploy src/test/resources/app.bar").toString()).
                contains("\"name\":\"app\"");
    }

    @AfterEach
    void deleteDeployment() {
        shell.evaluate(() -> "delete-deployments app");
        assertThat(shell.evaluate(() -> "lsd app").toString()).
                contains("\"size\":0");
    }

    @Test
    void startProcessInstanceCompleteTask() throws JsonProcessingException {
        Map<String, Object> processInstance = objectMapper.readValue(shell.evaluate(() -> "start oneTaskProcess").toString(), Map.class);
        assertThat(processInstance).containsKey("id");
        Map<String, Object> processes = objectMapper.readValue(shell.evaluate(() -> "lsp --instance-id " + processInstance.get("id")).toString(), Map.class);
        assertThat(processes.get("total")).isEqualTo(1);
        Map<String, Object> task = objectMapper.readValue(shell.evaluate(() -> "lst " + processInstance.get("id")).toString(), Map.class);
        assertThat(task.get("total")).isEqualTo(1);
        String taskId = (String) ((List<Map<String, Object>>) task.get("data")).get(0).get("id");
        shell.evaluate(() -> "tsk "+ taskId +" complete ");
        task = objectMapper.readValue(shell.evaluate(() -> "lst " + processInstance.get("id")).toString(), Map.class);
        assertThat(task.get("total")).isEqualTo(0);
        processes = objectMapper.readValue(shell.evaluate(() -> "lsp --instance-id" + processInstance.get("id")).toString(), Map.class);
        assertThat(processes.get("total")).isEqualTo(0);
    }
}