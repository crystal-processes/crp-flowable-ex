package org.crp.flowable.shell;

import org.crp.flowable.shell.commands.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes= Utils.class)
public class UtilsTest extends AbstractCommandTest {

    @Test
    void setFullConfiguration() {
        execute("configure login password restUrl idmUrl");
        assertScreenContainsText("login@restUrl/@idmUrl/");
        execute("configure --password password --rest-url restUrlUpdate");
        assertScreenContainsText("login@restUrl/@idmUrl/");
        execute("configure --designerUrl https://cloud.flowable.com/design/design-api");
        assertScreenContainsText("https://cloud.flowable.com/design/design-api");
        execute("configure admin test http://localhost:8080/flowable-ui/app-api/ --designerUrl https://cloud.flowable.com/design/design-api");
        assertScreenContainsText("admin@http://localhost:8080/flowable-ui/app-api");
    }

    @Test
    void zipUnZip() {
        execute("unzip src/test/resources/app.zip target/test/app");
        execute("zip target/test/app target/test-app.zip");

        File testZipFile = new File("target/test-app.zip");
        File sourceZipFile = new File("src/test/resources/app.zip");
        assertThat(testZipFile).hasSize(sourceZipFile.length());

        testZipFile.delete();
        (new File("target/test/app")).delete();
    }

}
