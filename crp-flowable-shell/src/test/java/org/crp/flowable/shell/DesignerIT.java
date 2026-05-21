package org.crp.flowable.shell;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class DesignerIT extends AbstractCommandTest {

    @Test
    void downloadModel() throws Exception {
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
    void downloadModelFull() throws Exception {
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
