package org.crp.flowable.form.impl;

import org.flowable.form.api.*;

import java.util.Map;

/**
 * Implement bare minimal Form service. Provide direct resource content,
 */
public class ResourceFormService implements FormService {
    @Override
    public void validateFormFields(String elementId, String elementType, String scopeId, String scopeDefinitionId, String scopeType, FormInfo formInfo, Map<String, Object> values) {

    }

    @Override
    public Map<String, Object> getVariablesFromFormSubmission(String elementId, String elementType, String scopeId, String scopeDefinitionId, String scopeType, FormInfo formInfo, Map<String, Object> values, String outcome) {
        return Map.of();
    }

    @Override
    public FormInstance createFormInstance(Map<String, Object> variables, FormInfo formInfo, String taskId, String processInstanceId, String processDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInstance saveFormInstance(Map<String, Object> variables, FormInfo formInfo, String taskId, String processInstanceId, String processDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInstance saveFormInstanceByFormDefinitionId(Map<String, Object> variables, String formDefinitionId, String taskId, String processInstanceId, String processDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInstance createFormInstanceWithScopeId(Map<String, Object> variables, FormInfo formInfo, String taskId, String scopeId, String scopeType, String scopeDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInstance saveFormInstanceWithScopeId(Map<String, Object> variables, FormInfo formInfo, String taskId, String scopeId, String scopeType, String scopeDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInstance saveFormInstanceWithScopeId(Map<String, Object> variables, String formDefinitionId, String taskId, String scopeId, String scopeType, String scopeDefinitionId, String tenantId, String outcome) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesById(String formDefinitionId, String taskId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesById(String formDefinitionId, String taskId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesByKey(String formDefinitionKey, String taskId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesByKey(String formDefinitionKey, String taskId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String taskId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInfo getFormModelWithVariablesByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String taskId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        FormInfo info = new FormInfo();
        FormModel formModel = new ResourceFormModel(formDefinitionKey);
        info.setFormModel(formModel);
        return info;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelById(String formInstanceId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelById(String formDefinitionId, String taskId, String processInstanceId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelById(String formDefinitionId, String taskId, String processInstanceId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKey(String formDefinitionKey, String taskId, String processInstanceId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKey(String formDefinitionKey, String taskId, String processInstanceId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String taskId, String processInstanceId, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String taskId, String processInstanceId, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndScopeId(String formDefinitionKey, String scopeId, String scopeType, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndScopeId(String formDefinitionKey, String scopeId, String scopeType, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndParentDeploymentIdAndScopeId(String formDefinitionKey, String parentDeploymentId, String scopeId, String scopeType, Map<String, Object> variables) {
        return null;
    }

    @Override
    public FormInstanceInfo getFormInstanceModelByKeyAndParentDeploymentIdAndScopeId(String formDefinitionKey, String parentDeploymentId, String scopeId, String scopeType, Map<String, Object> variables, String tenantId, boolean fallbackToDefaultTenant) {
        return null;
    }

    @Override
    public FormInstanceQuery createFormInstanceQuery() {
        return null;
    }

    @Override
    public byte[] getFormInstanceValues(String formInstanceId) {
        return new byte[0];
    }

    @Override
    public void deleteFormInstance(String formInstanceId) {

    }

    @Override
    public void deleteFormInstancesByFormDefinition(String formDefinitionId) {

    }

    @Override
    public void deleteFormInstancesByProcessDefinition(String processDefinitionId) {

    }

    @Override
    public void deleteFormInstancesByScopeDefinition(String scopeDefinitionId) {

    }
}
