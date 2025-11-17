package org.crp.flowable.mpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Service
public class FlowableService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowableService.class);


    @Tool(description = "List flowable user tasks for given credentials with basic authentication.")
    public String listUserTasks(String baseUrl, String userName, String password) {
        LOG.debug("Getting user tasks for user: {} and baseUrl: {}", userName, baseUrl);
        return postCall(getBasicAuthorizationRestClientBuilder(userName, password)
                .baseUrl(baseUrl.endsWith("/") + "process-api/query/tasks")
                .build()
        );
    }

    @Tool(description = "List flowable user tasks for given credentials with bearer token.")
    public String listUserTasks(String baseUrl, String bearerToken) {
        LOG.debug("Getting user tasks from baseUrl: {}", baseUrl);
        return postCall(getBearerAuthorizationRestClientBuilder(bearerToken)
                .baseUrl(baseUrl.endsWith("/") + "process-api/query/tasks")
                .build()
        );
    }

    private static String postCall(RestClient restClient) {
        return restClient.post()
                .retrieve()
                .body(String.class);
    }

    private static RestClient.Builder getBasicAuthorizationRestClientBuilder(String userName, String password) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()));
    }
    private static RestClient.Builder getBearerAuthorizationRestClientBuilder(String bearerToken) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
}
