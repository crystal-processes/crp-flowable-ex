package org.crp.flowable.shell;

import org.assertj.core.api.Condition;
import org.crp.flowable.shell.configuration.FlowableShellConfiguration;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.configuration.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellScreen;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@ContextConfiguration(classes={FlowableShellApplication.class, TestConfiguration.class, FlowableShellConfiguration.class, FlowableShellProperties.class})
public class AbstractCommandTest {

    @Autowired
    protected ShellTestClient client;

    protected ShellScreen lastScreen;

    protected void assertScreenContainsText(String text) {
        assertScreenContainsText(lastScreen, text);
    }

    protected ShellScreen execute(String command) throws Exception {
        lastScreen = client.sendCommand(command);
        return lastScreen;
    }

    protected void assertScreenContainsText(ShellScreen screen, String text) {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                ShellAssertions.assertThat(screen).containsText(text));
    }

    protected void assertScreenNotContainsText(String textFound, String textNotFound) {
        assertScreenNotContainsText(lastScreen, textFound, textNotFound);
    }

    protected void assertScreenNotContainsText(ShellScreen screen, String textFound, String textNotFound) {
        Condition<String> notCondition = new Condition<>(line -> line.contains(textNotFound), String.format("Text '%s' not found", textNotFound));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(screen).containsText(textFound);
            List<String> lines = screen.lines();
            assertThat(lines).areNot(notCondition);
        });
    }

}
