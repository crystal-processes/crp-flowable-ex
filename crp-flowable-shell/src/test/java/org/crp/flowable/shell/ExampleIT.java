package org.crp.flowable.shell;

import org.apache.commons.io.FileUtils;
import org.crp.flowable.shell.commands.Deployment;
import org.crp.flowable.shell.commands.Model;
import org.crp.flowable.shell.commands.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;


@ContextConfiguration(classes={Deployment.class, Model.class, Utils.class})
public class ExampleIT extends AbstractCommandTest {
    @Test
    void deployModel() throws IOException {
        //import app model to be able to export and deploy it
        execute("import --input-file-name src/test/resources/app.zip");

        //export model, unzip/zip bar, deploy
        execute("export-bar --name app --output-file-name target/test/app.bar");
        execute("unzip target/test/app.bar target/test/app");
        execute("zip target/test/app target/test/app-out.bar");
        execute("deploy target/test/app-out.bar");
        execute("lsd app-out");
        assertScreenContainsText("\"size\" : 1");

        //clean up
        execute("rmd app-out");
        execute("ls app");
        assertScreenContainsText("\"name\" : \"oneTaskProcess\"");
        execute("rm app");
        execute("ls app");
        assertScreenContainsText("\"size\" : 0");

        execute("ls oneTaskProcess bpmn");
        assertScreenContainsText("\"name\" : \"oneTaskProcess\"");
        execute("rm oneTaskProcess bpmn");
        execute("ls oneTaskProcess bpmn");
        assertScreenContainsText("\"size\" : 0");

        FileUtils.deleteDirectory(new File("target/test"));
    }

}
