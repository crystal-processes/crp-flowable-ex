package org.crp.flowable.shell;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelIT extends AbstractCommandTest {

    @Test
    void importExportApp() throws Exception {
        File outFile = new File("target/outputFile.zip");
        try {
            execute("import --input-file-name src/test/resources/app.zip");
            execute("list app");
            assertScreenContainsText("\"name\" : \"oneTaskProcess\"");

            execute("export --name app --output-file-name target/outputFile.zip");

            assertThat(outFile).exists();
        } finally {
            if (outFile.exists()) {
                if (!outFile.delete()) {
                    System.err.println("Unable to delete file");
                }
            }
            execute("rm app");
            execute("ls app");
            assertScreenNotContainsText("\"size\" : 0", "Error");

            execute("rm oneTaskProcess bpmn");
            execute("ls oneTaskProcess bpmn");
            assertScreenContainsText("\"size\" : 0");

        }
    }
}
