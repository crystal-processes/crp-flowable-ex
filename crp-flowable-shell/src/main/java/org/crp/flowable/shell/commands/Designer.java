package org.crp.flowable.shell.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.model.ShellCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@ShellCommandGroup
@ShellComponent
public class Designer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Designer.class);
    private static final String FLOWABLE_JSON_PROP_MESSAGE = "message";
    private static final String FLOWABLE_JSON_PROP_RESULT = "result";
    private static final String FLOWABLE_JSON_PROP_EXCEPTION = "exception";

    @Autowired
    protected ObjectMapper objectMapper;
    private final FlowableShellProperties properties;

    public Designer(FlowableShellProperties properties) {
        this.properties = properties;
    }

    @ShellMethod(value = "Export application model from modeler to file.", key = {"dx", "designer-export"})
    public JsonNode export(@ShellOption String name,
                       @ShellOption(value="workspace", defaultValue = "default") String workspace,
                       @ShellOption(value="output-file-name") String outputFileName) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            URI uri = new URIBuilder(createExportModelUrl(workspace, name)).build();
            HttpGet httpGet = new HttpGet(uri);
            addAuthorization(httpGet);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                if (httpResponse.getStatusLine().getStatusCode()>199 && httpResponse.getStatusLine().getStatusCode() < 300) {
                    try(InputStream content = httpResponse.getEntity().getContent()) {
                        File outputFile = createOutputFile(outputFileName, name);
                        FileUtils.copyInputStreamToFile(content, outputFile);
                        return objectMapper.createObjectNode().put("result", "ok").put("message", "Model " + workspace + "/" +name + " stored in "+outputFile.getCanonicalPath());
                    }
                } else {
                    int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
                    LOGGER.error("Unable to complete rest call errorCode {}", httpStatusCode);
                    try(InputStream content = httpResponse.getEntity().getContent()) {
                        JsonNode response = objectMapper.readTree(content);
                        if (response.has(FLOWABLE_JSON_PROP_MESSAGE) && response.has(FLOWABLE_JSON_PROP_EXCEPTION)) {
                            throw new ShellCommandException(response.get(FLOWABLE_JSON_PROP_MESSAGE).asText() + "\n" + response.get(FLOWABLE_JSON_PROP_EXCEPTION).asText());
                        } else {
                            throw new ShellCommandException(String.format("Unable to export model: \nuri=%s\nhttpStatusCode=%s\nresponseBody=%s", uri, httpStatusCode, IOUtils.toString(content, StandardCharsets.UTF_8)));
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new ShellCommandException("Unable to export model", e);
        }
    }

    private void addAuthorization(HttpRequestBase httpRequest) {
        if (!StringUtils.hasLength(properties.getToken())) {
            throw new ShellCommandException("Access token not set. Please set 'FLOWABLE_TOKEN' system variable or 'crp.flowable.shell.token' property.");
        }
        httpRequest.addHeader("Authorization", "Bearer "+ properties.getToken());
    }

    private static File createOutputFile(String outputFileName, String name) {
        return new File(StringUtils.hasText(outputFileName)? outputFileName : name+".zip");
    }

    private String createExportModelUrl(String workspace, String name) {
        return properties.getDesignerURL() + "/workspaces/" + workspace + "/apps/" + name + "/export";
    }

}
