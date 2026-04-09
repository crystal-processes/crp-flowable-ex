package org.crp.flowable.form.impl;

import org.crp.flowable.form.RawFormModel;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.form.api.FormInfo;
import org.flowable.rest.service.api.FormHandlerRestApiInterceptor;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;

public class RawFormHandlerInterceptor implements FormHandlerRestApiInterceptor {
    @Override
    public String convertStartFormInfo(FormInfo formInfo, ProcessDefinition processDefinition) {
        return getDirectFormModelContent(formInfo);
    }

    @Override
    public String convertTaskFormInfo(FormInfo formInfo, Task task) {
        return getDirectFormModelContent(formInfo);
    }

    @Override
    public String convertHistoricTaskFormInfo(FormInfo formInfo, HistoricTaskInstance task) {
        return getDirectFormModelContent(formInfo);
    }

    private static String getDirectFormModelContent(FormInfo formInfo) {
        if (formInfo.getFormModel() instanceof RawFormModel rawFormModel) {
            return rawFormModel.getForm();
        } else {
            throw new FormEngineException("Unable to recognize formInfo model type " + formInfo.getFormModel() + ". The supported type is RawFormModel.");
        }
    }
}
