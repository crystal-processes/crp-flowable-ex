package org.crp.flowable.shell;

import org.junit.jupiter.api.Test;

public class DeploymentIT extends AbstractCommandTest {

    @Test
    void deploy() throws Exception {
        try {
            execute("deploy src/test/resources/app.bar");
            execute("lsd app");
            assertScreenContainsText("\"size\" : 1");
        } finally {
            deleteDeployment("app");
        }
    }


    @Test
    void deployWithAppName() throws Exception {
        try {
            execute("deploy src/test/resources/app.bar --deployment-name testFileName.bar");
            execute("lsd testFileName");
            assertScreenContainsText("\"size\" : 1");
        } finally {
            deleteDeployment("testFileName");
        }
    }

    @Test
    void deployWithTenant() throws Exception {
        try {
            execute("deploy src/test/resources/app.bar --deployment-name app.bar --tenant-id testTenant");
            execute( "lsd app");
            assertScreenContainsText("\"tenantId\" : \"testTenant\"");
        } finally {
            deleteDeployment( "app");
        }
    }

    @Test
    void deployWithoutFileName() throws Exception {
        execute("deploy");
        assertScreenContainsText("Missing mandatory option '--path-to-application'");
    }

    private void deleteDeployment(String deploymentName) throws Exception {
        execute("delete-deployments " + deploymentName);
        execute("lsd "+ deploymentName);
        assertScreenContainsText("\"size\" : 0");
    }
}
