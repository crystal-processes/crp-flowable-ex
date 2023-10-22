package org.crp.flowable.shell.commands;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
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
import java.net.URISyntaxException;
import java.nio.file.Paths;

@ShellCommandGroup
@ShellComponent
public class Deployment extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployment.class);

    private final FlowableShellProperties properties;

    public Deployment(FlowableShellProperties properties) {
        this.properties = properties;
    }

    @ShellMethod("Deploy given application")
    public JsonNode deploy(@ShellOption(value= "path-to-application", optOut = true) String pathToApplication,
                           @ShellOption(value= "deployment-name", defaultValue = "") String deploymentName,
                           @ShellOption(value= "tenant-id", defaultValue = "") String tenantId) {
        String mandatoryFileName = StringUtils.isEmpty(deploymentName) ? Paths.get(pathToApplication).getFileName().toString() : deploymentName;

        return executeWithClient(client -> {
            HttpPost httpPost = new HttpPost(shellProperties.getRestURL() + properties.getDeploymentDeploy());
            return uploadFile(client, pathToApplication, mandatoryFileName, mandatoryFileName, tenantId, httpPost);
        });
    }

    @ShellMethod(value = "Delete all deployments with given name, tenantId from runtime. WARNING - use only for testing purposes",
            key ={"rmd", "delete-deployments"})
    public void deleteDeployments(String name, @ShellOption(defaultValue = "") String tenantId) {
        executeWithClient(client -> deleteDeployments(client, name, tenantId));
    }

    @ShellMethod(value = "list deployments", key = {"list-deployments", "lsd"})
    public JsonNode listDeployments(@ShellOption(defaultValue = "") String name, @ShellOption(defaultValue = "") String tenantId) {
        return executeWithClient(client -> getDeployments(client, name, tenantId));
    }

    protected void deleteDeployment(CloseableHttpClient client, String deploymentId){
        try {
            LOGGER.info("Deleting deployment id {}.", deploymentId);
            URIBuilder uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getDeploymentDeploy() + "/" + deploymentId);
            HttpDelete httpDelete = new HttpDelete(uriBuilder.build());
            try (CloseableHttpResponse response = executeBinaryRequest(client, httpDelete, false)) {
                LOGGER.info("Delete response {}", response.getStatusLine());
            } catch (IOException e) {
                LOGGER.error("Unable to deleteDeployment.", e);
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to deleteDeployment.", e);
        }
    }

    protected JsonNode deleteDeployments(CloseableHttpClient client, String name, @ShellOption(defaultValue = "") String tenantId) {
        JsonNode deployments = getDeployments(client, name, tenantId);
        int deploymentsSize = deployments.get("size").asInt();
        if (deploymentsSize == 0) {
            LOGGER.error("No deployment found name {}.", name);
            throw new RuntimeException("No deployment found " + name);
        }

        try {
            if (loginToApp(client)) {
                JsonNode data = deployments.get("data");
                for (JsonNode deployment : data) {
                    deleteDeployment(client, deployment.get("id").asText());
                }
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to save model to file", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    protected JsonNode getDeployments(CloseableHttpClient client, String name, String tenantId) {
        URIBuilder uriBuilder;
        HttpGet httpGet;
        try {
            uriBuilder = new URIBuilder(shellProperties.getRestURL() + properties.getDeploymentDeploy()).
                    addParameter("sort", "deployTime").
                    addParameter("order", "desc");
            if (!StringUtils.isEmpty(name)) {
                uriBuilder.addParameter("nameLike", name);
            }
            if (!StringUtils.isEmpty(tenantId)) {
                uriBuilder.addParameter("tenantId", tenantId);
            }
            httpGet = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Calling flowable rest api {} to get deployments", httpGet.getURI().toString());
        try (CloseableHttpResponse response = executeBinaryRequest(client, httpGet, true)) {
            return readContent(response);
        } catch (IOException e) {
            LOGGER.error("Unable get deployments", e);
            throw new RuntimeException(e);
        }
    }

}
