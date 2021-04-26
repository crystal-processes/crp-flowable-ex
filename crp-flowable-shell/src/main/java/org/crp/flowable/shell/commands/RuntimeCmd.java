package org.crp.flowable.shell.commands;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.utils.RestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;

@ShellCommandGroup
@ShellComponent
public class RuntimeCmd extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeCmd.class);

    private final FlowableShellProperties properties;

    public RuntimeCmd(FlowableShellProperties properties) {
        this.properties = properties;
    }

    @ShellMethod(value = "Start process or case")
    public JsonNode start(@ShellOption String definitionKey, @ShellOption(defaultValue = "") String variables) {
        return executeWithLoggedInClient(client -> {
            HttpPost httpPost = new HttpPost(configuration.getRestURL() + properties.getRuntime().getStartProcess());
            httpPost.setEntity(getStartProcessEntity(definitionKey, variables));
            try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpPost, true)) {
                if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                    throw new RuntimeException("Process instance was not created");
                }
                return readContent(closeableHttpResponse);
            } catch (IOException e) {
                LOGGER.error("Unable to start the process.", e);
                throw new UncheckedIOException(e);
            }
        });
    }

    @ShellMethod(value = "List process instances", key = {"lsp", "list-processes"})
    public JsonNode listProcesses(@ShellOption(defaultValue = "") String definitionKey,
                                  @ShellOption(defaultValue = "") String instanceId) {
        return executeWithLoggedInClient(client -> {
            HttpPost httpPost = new HttpPost(configuration.getRestURL() + properties.getRuntime().getQueryProcesses());
            httpPost.setEntity(getQueryProcessEntity(definitionKey, instanceId));
            try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpPost, true)) {
                return readContent(closeableHttpResponse);
            } catch (IOException e) {
                LOGGER.error("Unable to start the process.", e);
                throw new UncheckedIOException(e);
            }
        });
    }

    @ShellMethod(value = "List tasks", key = {"lst", "list-tasks"})
    public JsonNode listTasks(@ShellOption(defaultValue = "") String processInstanceId) {
        return executeWithLoggedInClient(client -> {
            HttpPost httpPost = new HttpPost(configuration.getRestURL() + properties.getRuntime().getQueryTasks());
            httpPost.setEntity(getQueryTaskEntity(processInstanceId));
            try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpPost, true)) {
                return readContent(closeableHttpResponse);
            } catch (IOException e) {
                LOGGER.error("Unable to list tasks.", e);
                throw new UncheckedIOException(e);
            }
        });
    }

    @ShellMethod(value = "Perform task action", key = {"task", "tsk"})
    public void task(String taskId, String action) {
        executeWithLoggedInClient(client -> {
            HttpPost httpPost = new HttpPost(configuration.getRestURL() + properties.getRuntime().getTaskAction() + "/" + taskId);
            httpPost.setEntity(getTaskActionEntity(action));
            try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpPost, true)) {
                if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("Unable to perfom action.");
                }
            } catch (IOException e) {
                LOGGER.error("Unable to list tasks.", e);
                throw new UncheckedIOException(e);
            }
            return null;
        });
    }

    private HttpEntity getTaskActionEntity(String action) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"action\": \"").append(action).append("\"}");
        try {
            return new StringEntity(sb.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpEntity getQueryProcessEntity(String definitionKey, String instanceId) {
        StringBuilder sb = new StringBuilder("{");
        if (!StringUtils.isEmpty(definitionKey)) {
            sb.append("\"processDefinitionKey\":\"" + definitionKey + "\"");
        }
        if (!StringUtils.isEmpty(instanceId)) {
            sb.append("\"processInstanceId\":\"" + instanceId + "\"");
        }
        sb.append("}");
        try {
            return new StringEntity(sb.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpEntity getQueryTaskEntity(String processInstanceId) {
        StringBuilder sb = new StringBuilder("{");
        if (!StringUtils.isEmpty(processInstanceId)) {
            sb.append("\"processInstanceId\":\"").append(processInstanceId).append("\"");
        }
        sb.append("}");
        try {
            return new StringEntity(sb.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpEntity getStartProcessEntity(String definitionKey, String variables) {
        try {
            StringBuilder sb = new StringBuilder("{\"processDefinitionKey\": \"" + definitionKey + "\"");
            if (!StringUtils.isEmpty(variables)) {
                sb.append(", \"variables\":\"").append(variables).append("\"");
            }
            sb.append("}");
            return new StringEntity(sb.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
