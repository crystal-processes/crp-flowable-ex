package org.crp.flowable.shell.commands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.utils.RestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class RawRest extends RestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawRest.class);

    private final FlowableShellProperties properties;

    public RawRest(FlowableShellProperties properties) {
        this.properties = properties;
    }

    @Command(name = {"exl", "execute-logged"}, description = "execute url with logged in client.")
    public JsonNode executeLoggedIn(@Option(defaultValue = "GET") RequestMethod method, String url,
            @Option(defaultValue = "") String body) {

        return executeWithLoggedInClient(client -> {
            try {
                HttpUriRequest httpUriRequest = createHttpUriRequest(url, method, body);
                try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpUriRequest, true)) {
                    return readContent(closeableHttpResponse);
                } catch (IOException e) {
                    LOGGER.error("Unable to execute request", e);
                    throw new RuntimeException(e);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Command(name = {"ex", "execute"}, description = "execute url.")
    public JsonNode execute(@Option(defaultValue = "GET") RequestMethod method, String url,
            @Option(defaultValue = "") String body) {
        return executeWithClient(client -> {
            try {
                HttpUriRequest httpUriRequest = createHttpUriRequest(url, method, body);
                try (CloseableHttpResponse closeableHttpResponse = executeBinaryRequest(client, httpUriRequest, true)) {
                    return readContent(closeableHttpResponse);
                } catch (IOException e) {
                    LOGGER.error("Unable to execute request", e);
                    throw new RuntimeException(e);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private HttpUriRequest createHttpUriRequest(String url, RequestMethod method, String body) throws UnsupportedEncodingException {

        switch (method) {
            case GET :
                return new HttpGet(shellProperties.getRestURL() + url);
            case DELETE :
                return new HttpDelete(shellProperties.getRestURL() + url);
            case POST :
                HttpPost httpPost = new HttpPost(shellProperties.getRestURL() + url);
                if (StringUtils.hasText(body)) {
                    httpPost.setEntity(new StringEntity(body));
                }
                return httpPost;
            case PUT :
                HttpPut httpPut = new HttpPut(shellProperties.getRestURL() + url);
                if (StringUtils.hasText(body)) {
                    httpPut.setEntity(new StringEntity(body));
                }
                return httpPut;
            default :
                LOGGER.error("");
                throw new RuntimeException("Unsupported method " + method + ", allowed method are " + Arrays.toString(RequestMethod.values()));
        }
    }

    public enum RequestMethod {
        GET, POST, PUT, DELETE
    }
}
