package org.crp.flowable.ai.delegates;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;

import java.util.List;

public class ChatClientJavaDelegate implements JavaDelegate {
    private  static final Logger LOG = LoggerFactory.getLogger(ChatClientJavaDelegate.class);

    protected Expression chatClient;
    protected Expression system;
    protected Expression user;
    protected Expression advisors;
    protected Expression structuredOutputConverter;
    protected Expression resultVariableName;
    protected Expression isTransient;

    @Override
    public void execute(DelegateExecution execution) {
        LOG.debug("Executing chat client call");

        new ChatClientCallBuilder(execution).call();

        LOG.debug("Chat client call was executed");
    }

    private class ChatClientCallBuilder {
        private final DelegateExecution execution;
        private final InputValues inputs;

        public ChatClientCallBuilder(DelegateExecution execution) {
            this.execution = execution;
            this.inputs = validatedInputs(execution);
        }

        public void call() {
            storeResultsFor( chatClientCall() );
        }

        protected void storeResultsFor(Object responseEntity) {
            if (inputs.isTransient()) {
                LOG.debug("Storing chat client call results in transient variable {}", inputs.resultVariableName());
                execution.setTransientVariable(inputs.resultVariableName(), responseEntity);
            } else {
                LOG.debug("Storing chat client call results in persistent variable {}", inputs.resultVariableName());
                execution.setVariable(inputs.resultVariableName(), responseEntity);
            }
        }

        protected Object chatClientCall() {
            return structureOutputFor(
                    callChatClient()
            );
        }

        private ChatClient.CallResponseSpec callChatClient() {
            var requestSpec = inputs.chatClient().prompt()
                    .system(inputs.system())
                    .user(inputs.user());
            if (!inputs.advisors().isEmpty()) {
                requestSpec.advisors(inputs.advisors());
            }
            return requestSpec.call();
        }

        private Object structureOutputFor(ChatClient.CallResponseSpec callResponse) {
            Object chatResponseEntity;
            if (inputs.structuredOutputConverter() != null) {
                chatResponseEntity = callResponse.entity(inputs.structuredOutputConverter());
            } else {
                chatResponseEntity = callResponse.content();
            }
            return chatResponseEntity;
        }

        protected InputValues validatedInputs(DelegateExecution execution) {
            return new InputValues(
                    ExpressionsHelper.getMandatoryValue("chatClient", chatClient, execution, ChatClient.class),
                    ExpressionsHelper.getMandatoryValue("system",system, execution, String.class),
                    ExpressionsHelper.getMandatoryValue("user", user, execution, String.class),
                    ExpressionsHelper.getValue(structuredOutputConverter, execution, new MapOutputConverter(), StructuredOutputConverter.class),
                    ExpressionsHelper.getMandatoryValue("ResultVariableName", resultVariableName, execution, String.class),
                    ExpressionsHelper.getValue(isTransient, execution, false, Boolean.class),
                    ExpressionsHelper.getValue(advisors, execution, List.of(), List.class)
                    );
        }

    }
}

record InputValues(ChatClient chatClient, String system, String user, StructuredOutputConverter<?> structuredOutputConverter,
                   String resultVariableName, boolean isTransient, List<Advisor> advisors) {}
