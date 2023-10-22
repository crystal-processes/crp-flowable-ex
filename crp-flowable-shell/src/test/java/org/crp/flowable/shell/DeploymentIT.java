package org.crp.flowable.shell;

import org.crp.flowable.shell.commands.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes={Deployment.class})
public class DeploymentIT extends AbstractCommandTest {

    @Test
    void deploy() {
        try {
            execute("deploy src/test/resources/app.bar");
            execute("lsd app");
            assertScreenContainsText("\"size\" : 1");
        } finally {
            deleteDeployment("app");
        }
    }


    @Test
    void deployWithAppName() {
        try {
            execute("deploy src/test/resources/app.bar --deployment-name testFileName.bar");
            execute("lsd testFileName");
            assertScreenContainsText("\"size\" : 1");
        } finally {
            deleteDeployment("testFileName");
        }
    }

    @Test
    void deployWithTenant() {
        try {
            execute("deploy src/test/resources/app.bar --deployment-name app.bar --tenant-id testTenant");
            execute( "lsd app");
            assertScreenContainsText("\"tenantId\" : \"testTenant\"");
        } finally {
            deleteDeployment( "app");
        }
    }

    @Test
    void deployWithoutFileName() {
        execute("deploy");
        assertScreenContainsText("Missing mandatory option '--path-to-application'");
    }

    private void deleteDeployment(String deploymentName) {
        execute("delete-deployments " + deploymentName);
        execute("lsd "+ deploymentName);
        assertScreenContainsText("\"size\" : 0");
    }
}
