package org.crp.flowable.shell.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.utils.RestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import freemarker.template.TemplateException;

@ShellCommandGroup
@ShellComponent
public class TemplateProcessor extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProcessor.class);

    private final FlowableShellProperties properties;
    private final ObjectMapper objectMapper;
    private final freemarker.template.Configuration freeMarkerConfiguration;

    public TemplateProcessor(FlowableShellProperties properties, ObjectMapper objectMapper, FreeMarkerConfigurationFactoryBean freeMarkerConfiguration,
            freemarker.template.Configuration configuration) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.freeMarkerConfiguration = configuration;
    }

    @ShellMethod(value = "Generate test from flowable history.", key = {"gt", "generate-test"})
    public void generateTest(String processInstanceId, String className,
                           @ShellOption(defaultValue = "java") String type, @ShellOption String sourceDir) {

        ObjectNode processInstanceModel = createProcessInstanceModel(processInstanceId, className);

        try(Writer pageWriter = new OutputStreamWriter(new FileOutputStream(getFullFileName(sourceDir, className)), StandardCharsets.UTF_8)) {
            freeMarkerConfiguration.getTemplate("FlowableExjUnitTest.ftl").process(processInstanceModel, pageWriter);
        } catch (IOException |TemplateException e) {
            LOGGER.error("Unable to generate test ["+className+"]["+processInstanceId+"].", e);
        }
    }

    private String getFullFileName(String sourceDir, String className) {
        String dir = StringUtils.hasText(sourceDir) ? sourceDir : shellProperties.getSourceTestJavaDir();
        return dir+ClassUtils.convertClassNameToResourcePath(className)+".java";
    }

    private ObjectNode createProcessInstanceModel(String processInstanceId, String className) {
        JsonNode activities = getActivities(processInstanceId);
        JsonNode startActivity = getStartActivity(activities);
        ObjectNode model = objectMapper.createObjectNode();
        model.put("className", ClassUtils.getShortName(className));
        String packageName = ClassUtils.getPackageName(className);
        if (StringUtils.hasText(packageName)) {
            model.put("package", packageName);
        }
        model.set("startActivity", startActivity);
        model.put("processDefinitionKey", getProcessDefinitionKey(startActivity.get("processDefinitionId").asText()));
        model.set("taskActivities", getTaskActivities(activities));
        return model;
    }

    private ArrayNode getTaskActivities(JsonNode activities) {
        ArrayNode tasks = objectMapper.createArrayNode();
        for (JsonNode activity : activities.get("data")) {
            JsonNode activityType = activity.get("activityType");
            if (activityType != null && "userTask".equals(activityType.asText())) {
                tasks.add(activity);
            }
        }
        return tasks;
    }

    private JsonNode getStartActivity(JsonNode activities) {
        ArrayNode data = (ArrayNode) activities.get("data");
        for (JsonNode activity : data) {
            if ("startEvent".equals(activity.get("activityType").asText())) {
                return activity;
            }
        }
        throw new RuntimeException("StartEvent was not found.");
    }

    private JsonNode getActivities(String processInstanceId) {
        return executeWithClient(client -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getHistoricActivityInstances());
                uriBuilder.addParameter("processInstanceId", processInstanceId);
                HttpGet httpGet = new HttpGet(uriBuilder.build());
                try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpGet, true)) {
                    return readContent(closeableHttpResponse);
                }
            } catch (IOException | URISyntaxException e) {
                LOGGER.error("Unable to get history.", e);
                throw new RuntimeException(e);
            }
        });
    }

    private String getProcessDefinitionKey(String processDefinitionId) {
        return executeWithLoggedInClient(client -> {
            try {
                HttpGet httpGet = new HttpGet(shellProperties.getRestURL() + properties.getProcessDefinitions() +"/" +processDefinitionId);
                try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpGet, true)) {
                    return readContent(closeableHttpResponse);
                }
            } catch (IOException e) {
                LOGGER.error("Unable to get process definition.", e);
                throw new RuntimeException(e);
            }
        }).get("key").asText();
    }

}
