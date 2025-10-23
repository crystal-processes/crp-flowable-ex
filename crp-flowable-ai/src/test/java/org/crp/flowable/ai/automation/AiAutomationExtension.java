package org.crp.flowable.ai.automation;

import org.crp.flowable.ai.automation.impl.AiDynamicBpmnServiceImpl;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.test.DeploymentId;
import org.flowable.engine.test.FlowableExtension;
import org.flowable.engine.test.FlowableTestHelper;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.util.Set;

public class AiAutomationExtension extends FlowableExtension {
    private static final Set<Class<?>> SUPPORTED_PARAMETERS = Set.of(
            AiDynamicBpmnService.class
    );

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return SUPPORTED_PARAMETERS.contains(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        FlowableTestHelper flowableTestHelper = getTestHelper(extensionContext);
        if (parameterContext.isAnnotated(DeploymentId.class)) {
            return flowableTestHelper.getDeploymentIdFromDeploymentAnnotation();
        }

        Class<?> parameterType = parameterContext.getParameter().getType();
        if (AiDynamicBpmnService.class.equals(parameterType)) {
            return new AiDynamicBpmnServiceImpl((ProcessEngineConfigurationImpl) flowableTestHelper.getProcessEngine().getProcessEngineConfiguration());
        }
        throw new ParameterResolutionException("Could not find service " + parameterType);
    }

}
