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
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@CommandGroup(name = "Model")
@Component
public class Model extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

    private static final Map<String, String> MODEL_TYPES = Collections.unmodifiableMap(new HashMap<>() {{
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

    @Command(description = "Export model from modeler to file.")
    public void export(@Option(defaultValue = "app") String type,
                       @Option String name,
                       @Option(longName = "tenant-id", defaultValue = "") String tenantId,
                       @Option(longName = "output-file-name") String outputFileName) {
        executeWithModelId(type, name, (client, modelId) -> saveModelToFile(client, modelId, outputFileName));
    }

    @Command(description = "Export deployable model from modeler to file.")
    public void exportBar(@Option(defaultValue = "app") String type,
                       @Option String name,
                       @Option(longName = "tenant-id", defaultValue = "") String tenantId,
                       @Option(longName = "output-file-name") String outputFileName) {
        executeWithModelId(type, name, (client, modelId) -> saveModelToBarFile(client, modelId, outputFileName));
    }

    @Command(name = {"rm", "delete-model"}, description = "Delete model from modeler.")
    public JsonNode deleteModel(String name,
                            @Option(defaultValue = "app") String type,
                            @Option(longName = "tenant-id", defaultValue = "") String tenantId) {
        return executeWithModelId(type, name, this::deleteModel);
    }


    @Command(name = {"import"}, description = "Import file to modeler.")
    public JsonNode importToModeler(@Option(longName = "input-file-name", required = false) String inputFileName,
                                @Option(longName = "tenant-id", defaultValue = "") String tenantId) {
        return executeWithClient(client -> importApp(client, inputFileName, Paths.get(inputFileName).getFileName().toString(), tenantId));
    }

    @Command(name = {"ls", "list"}, description = "List models.")
    public JsonNode list(@Option(defaultValue = "") String name, @Option(defaultValue = "app") String type) {
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
