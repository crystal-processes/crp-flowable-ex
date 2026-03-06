package org.crp.flowable.form.impl;

import org.crp.flowable.form.FormEngine;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.AbstractEngineConfigurator;
import org.flowable.common.engine.impl.EngineDeployer;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.form.api.FormEngineConfigurationApi;

import java.util.List;

public class FormEngineConfigurator extends AbstractEngineConfigurator<FormEngine> {

    protected FormEngineConfiguration formEngineConfiguration;

    @Override
    protected List<EngineDeployer> getCustomDeployers() {
        return List.of();
    }

    @Override
    protected String getMybatisCfgPath() {
        return null;
    }

    @Override
    protected FormEngine buildEngine() {
        if (formEngineConfiguration == null) {
            throw new FormEngineException("FormEngineConfiguration must not be null.");
        }
        return formEngineConfiguration.buildEngine();
    }

    @Override
    protected List<Class<? extends Entity>> getEntityInsertionOrder() {
        return List.of();
    }

    @Override
    protected List<Class<? extends Entity>> getEntityDeletionOrder() {
        return List.of();
    }

    @Override
    public void configure(AbstractEngineConfiguration engineConfiguration) {
        if(formEngineConfiguration == null) {
            formEngineConfiguration = new FormEngineConfiguration();
        }
        initialiseCommonProperties(engineConfiguration, this.formEngineConfiguration);
        initEngine();
        initServiceConfigurations(engineConfiguration, this.formEngineConfiguration);
    }

    @Override
    public int getPriority() {
        return 3000000;
    }

    public FormEngineConfigurationApi getFormEngineConfiguration() {
        return formEngineConfiguration;
    }

    public FormEngineConfigurator setFormEngineConfiguration(FormEngineConfiguration formEngineConfiguration) {
        this.formEngineConfiguration = formEngineConfiguration;
        return this;
    }
}
