package org.crp.flowable.ai.automation;

import org.crp.flowable.ai.automation.impl.DynamicUserTaskBuilderImpl;
import org.crp.flowable.ai.automation.impl.cmd.InjectUserTaskInExecutionCmd;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;

public class AiAutomationDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(AiAutomationDelegate.class);
    Expression dynamicBpmnService;
    Expression user;
    Expression chatClient;

    @Override
    public void execute(DelegateExecution execution) {
        String promptValue = (String) user.getValue(execution);
        ChatClient chatClientValue = (ChatClient) chatClient.getValue(execution);

        LOG.debug("Executing AI Automation Delegate with prompt: {} and chatClient: {}", promptValue, chatClient.getExpressionText());
        UserTask newUserTask = chatClientValue.prompt()
                .user(promptValue +
                        """
                        MUST: in structured JSON with keys : 
                        assignee - next user task assignee,
                        name - the task name,
                        id - the task id is shor meaningful abbreviation without spaces
                        
                        Always return only one task. If no other user task is needed, return an empty JSON object {}.
                        """)
                .call()
                .entity(UserTask.class);
        if (newUserTask != null && StringUtils.hasText(newUserTask.getId())) {
            LOG.debug("Injecting new User Task with id: {}, name: {}, assignee: {}", newUserTask.getId(), newUserTask.getName(), newUserTask.getAssignee());
            AiDynamicBpmnService dynamicBpmnServiceValue = (AiDynamicBpmnService) dynamicBpmnService.getValue(execution);
            dynamicBpmnServiceValue.createDynamicUserTaskBuilder()
                    // fill all attributes from newUserTask
                    .id(newUserTask.getId())
                    .name(newUserTask.getName())
                    .assignee(newUserTask.getAssignee())
                    .injectIntoExecution(execution.getId());
        } else {
            LOG.debug("No new User Task to inject.");
        }
    }
}
