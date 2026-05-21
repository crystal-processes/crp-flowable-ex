package org.crp.flowable.shell;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest extends AbstractCommandTest {

    @Test
    void setFullConfiguration() throws Exception {
        execute("configure --login login --password password --rest-url restUrl --idm-url idmUrl");
        assertScreenContainsText("login@restUrl/@idmUrl/");
        execute("configure --password password --rest-url restUrlUpdate");
        assertScreenContainsText("login@restUrlUpdate/@restUrlUpdate/");
        execute("configure --designerUrl https://cloud.flowable.com/design/design-api");
        assertScreenContainsText("https://cloud.flowable.com/design/design-api/");
        execute("configure --login admin --password test --rest-url http://localhost:8080/flowable-ui/app-api/ --designerUrl https://cloud.flowable.com/design/design-api");
        assertScreenContainsText("admin@http://localhost:8080/flowable-ui/app-api/");
    }

    @Test
    void zipUnZip() throws Exception {
        execute("unzip --zipFile src/test/resources/app.zip --targetDirectoryName target/test/app");
        execute("zip --sourceDirectory target/test/app --targetFileName target/test-app.zip");

        File testZipFile = new File("target/test-app.zip");
        File sourceZipFile = new File("src/test/resources/app.zip");
        assertThat(testZipFile).hasSize(sourceZipFile.length());

        testZipFile.delete();
        (new File("target/test/app")).delete();
    }

}
