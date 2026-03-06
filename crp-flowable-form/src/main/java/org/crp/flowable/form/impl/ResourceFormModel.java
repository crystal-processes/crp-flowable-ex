package org.crp.flowable.form.impl;

import org.apache.commons.io.IOUtils;
import org.crp.flowable.form.RawFormModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResourceFormModel implements org.flowable.form.api.FormModel, RawFormModel {

    private final String formDefinitionKey;

    public ResourceFormModel(String formDefinitionKey) {
        this.formDefinitionKey = formDefinitionKey;
    }

    @Override
    public String getForm() {
        try {
            return IOUtils.resourceToString(formDefinitionKey.startsWith("/") ? formDefinitionKey: "/"+formDefinitionKey, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FormEngineException("Unable to load form "+ formDefinitionKey, e);
        }
    }
}
