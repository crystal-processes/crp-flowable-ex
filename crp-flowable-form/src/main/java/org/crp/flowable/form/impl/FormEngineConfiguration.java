package org.crp.flowable.form.impl;

import org.crp.flowable.form.FormEngine;
import org.flowable.common.engine.impl.AbstractBuildableEngineConfiguration;
import org.flowable.common.engine.impl.db.SchemaManager;
import org.flowable.common.engine.impl.interceptor.CommandInterceptor;
import org.flowable.common.engine.impl.interceptor.EngineConfigurationConstants;
import org.flowable.form.api.FormEngineConfigurationApi;
import org.flowable.form.api.FormManagementService;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.form.api.FormService;

import java.io.InputStream;
import java.util.function.Consumer;

public class FormEngineConfiguration extends AbstractBuildableEngineConfiguration<FormEngine> implements FormEngineConfigurationApi {
    protected String formEngineName;
    protected FormService formService;

    @Override
    protected FormEngine createEngine() {
        return new FormEngineImpl(formEngineName, this);
    }

    @Override
    protected void init() {
        initEngineConfigurations();
        initConfigurators();
        configuratorsBeforeInit();
        initClock();
        initObjectMapper();
        initCommandContextFactory();
        initTransactionContextFactory();
        initCommandExecutors();
        initIdGenerator();
        initBeans();

        initTransactionFactory();

        if (usingRelationalDatabase) {
            initSqlSessionFactory();
        }

        initSessionFactories();
        initDataManagers();
        initEntityManagers();
        initEventDispatcher();

        configuratorsAfterInit();
    }

    @Override
    protected Consumer<FormEngine> createPostEngineBuildConsumer() {
        return formEngine -> {};
    }

    @Override
    protected SchemaManager createEngineSchemaManager() {
        return new SchemaManager() {
            @Override
            public void schemaCreate() {

            }

            @Override
            public void schemaDrop() {

            }

            @Override
            public String schemaUpdate() {
                return "";
            }

            @Override
            public void schemaCheckVersion() {

            }

            @Override
            public String getContext() {
                return null;
            }
        };
    }

    @Override
    public String getEngineCfgKey() {
        return EngineConfigurationConstants.KEY_FORM_ENGINE_CONFIG;
    }

    @Override
    public String getEngineScopeType() {
        return "form";
    }

    @Override
    public CommandInterceptor createTransactionInterceptor() {
        return null;
    }

    @Override
    protected void initDbSqlSessionFactoryEntitySettings() {

    }

    @Override
    public InputStream getMyBatisXmlConfigurationStream() {
        return null;
    }

    @Override
    public String getEngineName() {
        return formEngineName;
    }

    @Override
    public FormManagementService getFormManagementService() {
        return null;
    }

    @Override
    public FormRepositoryService getFormRepositoryService() {
        return null;
    }

    @Override
    public FormService getFormService() {
        return formService;
    }

    public String getFormEngineName() {
        return formEngineName;
    }

    public FormEngineConfiguration setFormEngineName(String formEngineName) {
        this.formEngineName = formEngineName;
        return this;
    }

    public FormEngineConfiguration setFormService(FormService formService) {
        this.formService = formService;
        return this;
    }
}
