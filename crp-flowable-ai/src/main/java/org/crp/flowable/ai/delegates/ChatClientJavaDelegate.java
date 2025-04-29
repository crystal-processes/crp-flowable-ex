package org.crp.flowable.ai.delegates;

import org.crp.flowable.ai.AiException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;

public class ChatClientJavaDelegate implements JavaDelegate {
    private  static final Logger LOG = LoggerFactory.getLogger(ChatClientJavaDelegate.class);

    protected Expression chatClient;
    protected Expression system;
    protected Expression user;
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
                    inputs.chatClient().prompt()
                            .system(inputs.system())
                            .user(inputs.user())
                            .call()
            );
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
                    (ChatClient) getMandatoryValue("chatClient", chatClient, execution),
                    (String) getMandatoryValue("system",system, execution),
                    (String) getMandatoryValue("user", user, execution),
                    (StructuredOutputConverter<?>) getValue(structuredOutputConverter, execution, new MapOutputConverter()),
                    (String) getMandatoryValue("ResultVariableName", resultVariableName, execution),
                    (boolean) getValue(isTransient, execution, false));
        }

        protected static Object getValue(Expression expression, DelegateExecution execution, Object defaultValue) {
            return expression != null ? expression.getValue(execution) : defaultValue;
        }

        protected static Object getValue(Expression expression, DelegateExecution execution) {
            return getValue(expression, execution, null);
        }

        protected static Object getMandatoryValue(String name, Expression expression, DelegateExecution execution) {
            Object value = getValue(expression, execution);
            if (value == null) {
                LOG.error("{} is mandatory.", name);
                throw new AiException(name + " is mandatory");
            }
            return value;
        }

    }
}

record InputValues(ChatClient chatClient, String system, String user, StructuredOutputConverter<?> structuredOutputConverter,
                   String resultVariableName, boolean isTransient) {}
