package org.crp.flowable.shell;

import org.assertj.core.api.Condition;
import org.crp.flowable.shell.configuration.FlowableShellConfiguration;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.crp.flowable.shell.configuration.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@ContextConfiguration(classes={TestConfiguration.class, FlowableShellConfiguration.class, FlowableShellProperties.class})
public class AbstractCommandTest {

    @Autowired
    protected ShellTestClient client;

    @Autowired
    protected EvaluableShell evaluableShell;

    protected ShellTestClient.InteractiveShellSession session;

    @BeforeEach
    void initializeSession() {
        session = client
                .interactive()
                .run();
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(session.screen().lines()).anySatisfy(line -> assertThat(line).contains("flowable-shell:>")));
    }

    protected void assertScreenContainsText(String text) {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                ShellAssertions.assertThat(session.screen()).containsText(text));
    }

    protected void execute(String command) {
        session.write(session.writeSequence().text(command).cr().build());
        assertScreenContainsText("flowable-shell:>");
    }

    protected void assertScreenNotContainsText(String textFound, String textNotFound) {
        Condition<String> notCondition = new Condition<>(line -> line.contains(textNotFound), String.format("Text '%s' not found", textNotFound));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen()).containsText(textFound);
            List<String> lines = session.screen().lines();
            assertThat(lines).areNot(notCondition);
        });
    }

}
