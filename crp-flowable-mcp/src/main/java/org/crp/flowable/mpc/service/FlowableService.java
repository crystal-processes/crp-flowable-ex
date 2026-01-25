package org.crp.flowable.mpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Service
public class FlowableService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowableService.class);


    @Tool(description = "List flowable user tasks for given credentials with basic authentication.")
    public String listUserTasks(String baseUrl, String userName, String password) {
        LOG.debug("Getting user tasks for user: {} and baseUrl: {}", userName, baseUrl);
        return postCall(getBasicAuthorizationRestClientBuilder(userName, password)
                .baseUrl(getUrl(baseUrl, "process-api/query/tasks"))
                .build(), "{}");
    }

    @Tool(description = "List flowable user tasks for given credentials with bearer token.")
    public String listUserTasksWithBearerToken(String baseUrl, String bearerToken) {
        LOG.debug("Getting user tasks from baseUrl: {}", baseUrl);
        return postCall(getBearerAuthorizationRestClientBuilder(bearerToken)
                .baseUrl(getUrl(baseUrl, "process-api/query/tasks"))
                .build(),
                "{}");
    }

    @Tool(description = "Complete user task with variables with bearerToken.")
    public String completeUserTaskWithBearer(String baseUrl, String bearerToken, String taskId, String outcome, Map<String, Object> variables) {
        LOG.debug("Completing user task {} from baseUrl: {}", taskId, baseUrl);
        return postCall(getBearerAuthorizationRestClientBuilder(bearerToken)
                .baseUrl(getUrl(baseUrl, "process-api/runtime/tasks/"+taskId))
                .build(),
                createCompleteTaskBody(variables)
        );
    }

    @Tool(description = "Complete user task with variables with basic authentication.")
    public String completeUserTaskWithBasicAuthentication(String baseUrl,  String userName, String password, String taskId, String outcome, Map<String, Object> variables) {
        LOG.debug("Completing user task {} from baseUrl: {}", taskId, baseUrl);
        return postCall(getBasicAuthorizationRestClientBuilder(userName, password)
                .baseUrl(getUrl(baseUrl, "process-api/runtime/tasks/"+taskId))
                .build(),
                createCompleteTaskBody(variables)
        );
    }

    @Tool(description = """
            Get flowable form data for user task or process definition (in case of starting process).
            """)
    public String formData(String baseUrl, String bearerToken, String taskId, String definitionId) {
        LOG.debug("Fetching form data ");
        throw new RuntimeException("notImplemented");
    }

    private static String createCompleteTaskBody(Map<String, Object> variables) {
        StringBuilder completeTaskBody = new StringBuilder("""
                {
                  "action": "complete"
                """);
        appendVariables(completeTaskBody, variables);
        completeTaskBody.append("\n}");
        return completeTaskBody.toString();
    }

    private static void appendVariables(StringBuilder completeTaskBody, Map<String, Object> variables) {
        variables.forEach((key, value) -> completeTaskBody
                .append(",\n\"")
                .append(key).append("\":")
                .append(getJsonValue(value)));
    }

    private static String getJsonValue(Object value) {
        return value instanceof String ? "\"" + value + "\"" : value.toString();
    }

    private static String getUrl(String baseUrl, String postfix) {
        return (baseUrl.endsWith("/") ? baseUrl : baseUrl +"/")  + postfix;
    }

    private static String postCall(RestClient restClient, String requestBody) {
        return restClient
                .post()
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    private static RestClient.Builder getBasicAuthorizationRestClientBuilder(String userName, String password) {
        return getBuilder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()));
    }

    private static RestClient.Builder getBuilder() {
        return RestClient.builder();
    }

    private static RestClient.Builder getBearerAuthorizationRestClientBuilder(String bearerToken) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
}
