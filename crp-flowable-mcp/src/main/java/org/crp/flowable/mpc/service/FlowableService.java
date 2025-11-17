package org.crp.flowable.mpc.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Service
public class FlowableService {

//    private static final Logger LOG = Logger.getLogger(FlowableService.class.getName());


    @Tool(name="listUserTasks",
            description = "List flowable user tasks for given credentials")
    public String listUserTasks(String baseUrl, String userName, String password) {
//        LOG.log(Level.FINEST, "Getting user tasks for user: {} and baseUrl: {}", new Object[]{userName, baseUrl});
        RestClient restClient = getBasicAuthorizationRestClientBuilder(userName, password)
                .baseUrl(baseUrl.endsWith("/") + "process-api/query/tasks")
                .build();

//        return restClient.post()
//                .retrieve()
//                .body(new ParameterizedTypeReference<>() {
//                });
        return "[{\"id\":\"task1\",\"name\":\"Task 1\"},{\"id\":\"task2\",\"name\":\"Task 2\"}]";
    }

    private static RestClient.Builder getBasicAuthorizationRestClientBuilder(String userName, String password) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()));
    }
}
