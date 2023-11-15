package org.crp.flowable.shell;

import org.apache.commons.io.FileUtils;
import org.crp.flowable.shell.commands.Designer;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes={FlowableShellProperties.class, Designer.class})
public class DesignerIT extends AbstractCommandTest {

    @Test
    void downloadModel() throws IOException {
        execute("dx test");
        assertScreenContainsText("test.zip");
        var expectedOutFile = new File("test.zip");
        try {
            assertThat(expectedOutFile).isNotEmpty();
        } finally {
            if (expectedOutFile.exists()) {
                FileUtils.delete(expectedOutFile);
            }
        }
    }

    @Test
    void downloadModelFull() throws IOException {
        execute("dx test default out.zip");
        assertScreenContainsText("result");
        var expectedOutFile = new File("out.zip");
        try {
            assertThat(expectedOutFile).isNotEmpty();
        } finally {
            if (expectedOutFile.exists()) {
                FileUtils.delete(expectedOutFile);
            }
        }
    }

}
