package org.crp.flowable.ai.automation;

public interface DynamicServiceTaskBuilder extends DynamicInjectionBuilder {
     DynamicServiceTaskBuilder id(String id);

     DynamicServiceTaskBuilder expression(String expression);

     DynamicServiceTaskBuilder resultVariableName(String resultVariableName);

}
