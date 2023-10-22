package org.crp.flowable.shell.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.jline.terminal.Terminal;
import org.springframework.shell.result.TerminalAwareResultHandler;

public class JsonNodeResultHandler extends TerminalAwareResultHandler<JsonNode> {

    public JsonNodeResultHandler(Terminal terminal) {
        super(terminal);
    }

    @Override
    protected void doHandleResult(JsonNode jsonNode) {
        this.terminal.writer().println(jsonNode.toPrettyString());
        this.terminal.writer().flush();
    }
}
