package org.crp.flowable.shell;

import org.junit.jupiter.api.Test;

public class RawRestIT extends AbstractCommandTest {
    @Test
    void deployModel() throws Exception {
        // given: deploy app
        execute("deploy src/test/resources/app.bar --deployment-name testFileName.bar");
        assertScreenContainsText("\"name\" : \"testFileName\"");


        // get
        execute("ex GET /app-api/app-repository/deployments");
        assertScreenContainsText("\"name\" : \"testFileName\"");
        // post
        execute("ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}");

    }

}
