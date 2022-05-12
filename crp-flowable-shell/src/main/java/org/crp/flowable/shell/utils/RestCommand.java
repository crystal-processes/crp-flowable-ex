package org.crp.flowable.shell.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.net.HttpURLConnection.HTTP_OK;

public class RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCommand.class);

    @Autowired
    protected FlowableShellProperties shellProperties;
    @Autowired
    protected ObjectMapper objectMapper;

    protected CloseableHttpClient createClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(shellProperties.getLogin(), shellProperties.getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);
        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    protected JsonNode uploadFile(CloseableHttpClient client, String pathToApplication, String file, String fileName, String tenantId, HttpPost httpPost) {
        try (InputStream fis = getApplicationInputStream(pathToApplication);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            httpPost.setEntity(
                    HttpMultipartHelper.getMultiPartEntity(file, fileName, "application/zip", bis, Collections.singletonMap("tenantId", tenantId)));
            LOGGER.info("Calling flowable rest api {} to deploy {} into tenantId {}", httpPost.getURI().toString(), pathToApplication, tenantId);
            try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpPost, false)) {
                return readContent(closeableHttpResponse);
            } catch (IOException e) {
                LOGGER.error("Unable to execute request", e);
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private InputStream getApplicationInputStream(String pathToApplication) throws FileNotFoundException {
        File appFile = new File(pathToApplication);
        if (appFile.exists()) {
            return new FileInputStream(pathToApplication);
        } else {
            return getClass().getResourceAsStream(pathToApplication);
        }
    }

    protected boolean loginToApp(CloseableHttpClient client) throws URISyntaxException {
        URIBuilder idmUriBuilder = new URIBuilder(shellProperties.getIdmURL() + "/app/authentication").
                addParameter("j_username", shellProperties.getLogin()).addParameter("j_password", shellProperties.getPassword()).
                addParameter("_spring_security_remember_me", "true").addParameter("submit", "Login");
        HttpPost appLogin = new HttpPost(idmUriBuilder.build());
        try (CloseableHttpResponse idmResponse = executeBinaryRequest(client, appLogin, false)) {
            if (idmResponse.getStatusLine().getStatusCode() != HTTP_OK) {
                LOGGER.error("Unable to establish connection to modeler app {}", idmResponse.getStatusLine());
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("unable to establish connection to modeler", e);
            throw new RuntimeException(e);
        }
    }

    protected JsonNode executeWithClient(ExecuteWithClient exec) {
        CloseableHttpClient client = createClient();
        try {
            return exec.execute(client);
        } finally {
            closeClient(client);
        }
    }

    protected void closeClient(CloseableHttpClient client) {
        try {
            client.close();
        } catch (IOException e) {
            LOGGER.error("Unable to close client", e);
        }
    }

    protected JsonNode executeWithLoggedInClient(ExecuteWithClient exec) {
        CloseableHttpClient client = createClient();
        try {
            if (loginToApp(client)) {
                return exec.execute(client);
            } else {
                LOGGER.error("Unable to login.");
                throw new RuntimeException("Unable to login");
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Login failed.", e);
            throw new RuntimeException(e);
        } finally {
            closeClient(client);
        }
    }

    protected CloseableHttpResponse executeBinaryRequest(CloseableHttpClient client, HttpUriRequest request, boolean addJsonContentType) {
        try {
            if (addJsonContentType && request.getFirstHeader(HttpHeaders.CONTENT_TYPE) == null) {
                // Revert to default content-type
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
            }
            return client.execute(request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected JsonNode readContent(CloseableHttpResponse response) {
        if (response.getStatusLine().getStatusCode() < 300 && response.getStatusLine().getStatusCode() >199) {
            try {
                if (response.getEntity() != null) {
                    return objectMapper.readTree(response.getEntity().getContent());
                }
                return objectMapper.createObjectNode();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new RuntimeException("The response is not correct " + response.getStatusLine());
        }
    }

    protected void closeResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
