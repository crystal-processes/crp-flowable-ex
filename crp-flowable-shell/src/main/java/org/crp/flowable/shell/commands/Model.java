package org.crp.flowable.shell.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.utils.ExecuteWithModelId;
import org.crp.flowable.shell.utils.RestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ShellCommandGroup
@ShellComponent
public class Model extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

    private static final Map<String, String> MODEL_TYPES = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("bpmn", "0");
        put("form", "2");
        put("app", "3");
        put("decision-table", "4");
        put("cmmn", "5");
        put("decision-service", "6");
    }});

    private final FlowableShellProperties properties;

    public Model(FlowableShellProperties properties) {
        this.properties = properties;
    }

    @ShellMethod("Export model from modeler to file.")
    public void export(@ShellOption(defaultValue = "app") String type,
                       @ShellOption String name,
                       @ShellOption(value="tenant-id", defaultValue = "") String tenantId,
                       @ShellOption(value="output-file-name") String outputFileName) {
        executeWithModelId(type, name, (client, modelId) -> saveModelToFile(client, modelId, outputFileName));
    }

    @ShellMethod("Export deployable model from modeler to file.")
    public void exportBar(@ShellOption(defaultValue = "app") String type,
                       @ShellOption String name,
                       @ShellOption(value="tenant-id", defaultValue = "") String tenantId,
                       @ShellOption(value="output-file-name") String outputFileName) {
        executeWithModelId(type, name, (client, modelId) -> saveModelToBarFile(client, modelId, outputFileName));
    }

    @ShellMethod(value = "Delete model from modeler.", key = {"rm", "delete-model"})
    public JsonNode deleteModel(String name,
                            @ShellOption(defaultValue = "app") String type,
                            @ShellOption(value="tenant-id", defaultValue = "") String tenantId) {
        return executeWithModelId(type, name, this::deleteModel);
    }


    @ShellMethod(value="Import file to modeler.", key="import")
    public JsonNode importToModeler(@ShellOption(value="input-file-name", optOut = true) String inputFileName,
                                @ShellOption(value="tenant-id", defaultValue = "") String tenantId) {
        return executeWithClient(client -> importApp(client, inputFileName, Paths.get(inputFileName).getFileName().toString(), tenantId));
    }

    @ShellMethod(value = "List models.", key={"ls", "list"})
    public JsonNode list(@ShellOption(defaultValue = "") String name, @ShellOption(defaultValue = "app") String type) {
        return executeWithClient(client -> getModels(client, type, name));
    }

    protected JsonNode saveModelToFile(CloseableHttpClient client, String modelId, String outputFileName) {
        return saveModelFromUrlToFile(client, properties.getModelerAppDefinitions() + modelId + properties.getModelerExport(), outputFileName);
    }

    protected JsonNode saveModelToBarFile(CloseableHttpClient client, String modelId, String outputFileName) {
        return saveModelFromUrlToFile(client, properties.getModelerAppDefinitions() + modelId + properties.getModelerExportBar(), outputFileName);
    }

    protected ObjectNode saveModelFromUrlToFile(CloseableHttpClient client, String url, String outputFileName) {
        try {
            URIBuilder uriBuilder = new URIBuilder(shellProperties.getRestURL() + url);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            LOGGER.info("Getting model from url {} to file {}.", uriBuilder.getPath(), outputFileName);
            try (CloseableHttpResponse response = executeBinaryRequest(client, httpGet, false)) {
                InputStream content = response.getEntity().getContent();
                FileUtils.copyInputStreamToFile(content, new File(outputFileName));
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Unable to save file.", e);
        }
        return objectMapper.createObjectNode();
    }

    protected JsonNode deleteModel(CloseableHttpClient client, String modelId){
        try {
            URIBuilder uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getModelerModels() + modelId);
            uriBuilder.addParameter("cascade", "true");
            HttpDelete httpDelete = new HttpDelete(uriBuilder.build());
            LOGGER.info("Deleting model id {}.", modelId);
            try (CloseableHttpResponse response = executeBinaryRequest(client, httpDelete, false)) {
                JsonNode responseNode = readContent(response);
                LOGGER.info("Delete response {}", response.getStatusLine());
                return responseNode;
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Unable to deleteModel.", e);
        }
        return null;
    }

    protected JsonNode importApp(CloseableHttpClient client, String pathToFile, String fileName, String tenantId){
        try {
            URIBuilder uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getModelerAppDefinitions() + properties.getModelerImport());
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            loginToApp(client);
            return uploadFile(client, pathToFile, "file", fileName, tenantId, httpPost);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to import model.", e);
        }
        return null;
    }

    protected JsonNode executeWithModelId(String type, String name, ExecuteWithModelId exec) {
        return executeWithClient(client -> {
            JsonNode responseNode = getModels(client, type, name);
            int modelsSize = responseNode.get("size").asInt();
            if (modelsSize > 1) {
                LOGGER.error("Ambiguous model name {} of type {}.", name, type);
                throw new RuntimeException("More than one model '" + name + "' returned [" + modelsSize + "]");
            }
            if (modelsSize == 0) {
                LOGGER.error("No model found name {} of type {}.", name, type);
                throw new RuntimeException("No model found " + name);
            }

            String modelId = responseNode.get("data").get(0).get("id").asText();
            try {
                if (loginToApp(client)) {
                    return exec.execute(client, modelId);
                }
            } catch (URISyntaxException e) {
                LOGGER.error("Unable to save model to file", e);
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    protected JsonNode getModels(CloseableHttpClient client, String type, String name) {
        URIBuilder uriBuilder;
        HttpGet httpGet;
        try {
            uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getModelerEditorModels()).
                    addParameter("modelType", getModelType(type)).
                    addParameter("filterText", name).
                    addParameter("sort", "modifiedDesc");
            httpGet = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Calling flowable rest api {} to get models", httpGet.getURI().toString());
        try (CloseableHttpResponse response = executeBinaryRequest(client, httpGet, true)) {
            return readContent(response);
        } catch (IOException e) {
            LOGGER.error("Unable to get models", e);
            throw new RuntimeException("Unable to get models", e);
        }
    }

    protected String getModelType(String type) {
        if (!MODEL_TYPES.containsKey(type)) {
            throw new IllegalArgumentException("Parameter type " + type + " is not supported. Valid parameter types are " + MODEL_TYPES.keySet() + ".");
        }
        return MODEL_TYPES.get(type);
    }

}
