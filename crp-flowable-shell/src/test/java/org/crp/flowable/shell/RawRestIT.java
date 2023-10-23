package org.crp.flowable.shell;

import org.crp.flowable.shell.commands.Deployment;
import org.crp.flowable.shell.commands.RawRest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes= {Deployment.class, RawRest.class})
public class RawRestIT extends AbstractCommandTest {
    @Test
    void deployModel() {
        // given: deploy app
        execute("deploy src/test/resources/app.bar --deployment-name testFileName.bar");
        assertScreenContainsText("\"name\" : \"testFileName\"");


        // get
        execute("ex GET /app-api/app-repository/deployments");
        assertScreenContainsText("\"name\" : \"testFileName\"");
        // post
        execute("ex POST /process-api/runtime/process-instances {\"processDefinitionKey\":\"oneTaskProcess\",\"businessKey\":\"myBusinessKey\"}");

        // put
        // execute("exl PUT /process-api/runtime/process-instances/"+processId+" {\"name\":\"nameone\"}").toString()).contains("nameone");

        // delete
        //execute("ex DELETE /app-api/app-repository/deployments/"+deploymentId).toString()).isEqualTo("{}");

    }

}
