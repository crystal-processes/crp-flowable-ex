package org.crp.flowable.form.impl;

import org.crp.flowable.form.FormEngine;
import org.flowable.form.api.FormEngineConfigurationApi;

public class FormEngineImpl implements FormEngine {

    protected final FormEngineConfigurationApi formEngineConfiguration;
    protected final String name;

    public FormEngineImpl(String name, FormEngineConfigurationApi formEngineConfiguration) {
        this.name = name;
        this.formEngineConfiguration = formEngineConfiguration;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void close() {
    }
}
