package org.crp.flowable.mcp.config;

import org.flowable.cmmn.api.*;
import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.cmmn.engine.CmmnEngineConfiguration;
import org.flowable.cmmn.engine.CmmnEngines;
import org.flowable.cmmn.spring.SpringCmmnEngineConfiguration;
import org.flowable.cmmn.spring.configurator.SpringCmmnEngineConfigurator;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.idm.spring.configurator.SpringIdmEngineConfigurator;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.h2.Driver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.function.Consumer;

@Configuration
@Import(CrpFlowableMcpToolsConfiguration.class)
public class TestConfiguration {

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUrl("jdbc:h2:mem:flowable-jupiter;DB_CLOSE_DELAY=1000");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ProcessEngineFactoryBean processEngineFactoryBean(ProcessEngineConfigurationImpl processEngineConfiguration) {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration);

        return factoryBean;
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager,
                                                                     ObjectProvider<Consumer<ProcessEngineConfiguration>> processEngineConfigurators) {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngineConfiguration.setTransactionManager(transactionManager);
        processEngineConfiguration.setAsyncExecutorActivate(false);
        processEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
        processEngineConfiguration.setEnableEntityLinks(true);

        processEngineConfigurators.orderedStream()
                .forEach(consumer -> consumer.accept(processEngineConfiguration));

        return processEngineConfiguration;
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }


    @Bean
    public CmmnEngineConfiguration cmmnEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager) {
        SpringCmmnEngineConfiguration cmmnEngineConfiguration = new SpringCmmnEngineConfiguration();
        cmmnEngineConfiguration.setDataSource(dataSource);
        cmmnEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        cmmnEngineConfiguration.setTransactionManager(transactionManager);
        cmmnEngineConfiguration.setAsyncExecutorActivate(false);
        cmmnEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
        cmmnEngineConfiguration.setEnableEntityLinks(true);

        return cmmnEngineConfiguration;
    }

    @Bean
    public CmmnEngine cmmnEngine(@SuppressWarnings("unused") ProcessEngine processEngine) {
        // The process engine needs to be injected, as otherwise it won't be initialized, which means that the CmmnEngine is not initialized yet
        if (!CmmnEngines.isInitialized()) {
            throw new IllegalStateException("cmmn engine has not been initialized");
        }
        return CmmnEngines.getDefaultCmmnEngine();
    }

    @Bean
    public SpringCmmnEngineConfigurator cmmnEngineConfigurator(CmmnEngineConfiguration cmmnEngineConfiguration) {
        SpringCmmnEngineConfigurator engineConfigurator = new SpringCmmnEngineConfigurator();
        engineConfigurator.setCmmnEngineConfiguration(cmmnEngineConfiguration);
        return engineConfigurator;
    }

    @Bean
    public Consumer<ProcessEngineConfiguration> cmmnProcessEngineConfigurator(SpringCmmnEngineConfigurator cmmnEngineConfigurator) {
        return processEngineConfiguration -> {
            processEngineConfiguration.addConfigurator(cmmnEngineConfigurator);
        };
    }
    @Bean
    public SpringIdmEngineConfigurator idmEngineConfigurator() {
        return new SpringIdmEngineConfigurator();
    }

    @Bean
    public CmmnRepositoryService cmmnRepositoryService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnRepositoryService();
    }

    @Bean
    public CmmnMigrationService cmmnMigrationService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnMigrationService();
    }

    @Bean
    public CmmnRuntimeService cmmnRuntimeService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnRuntimeService();
    }

    @Bean
    public CmmnTaskService cmmnTaskService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnTaskService();
    }

    @Bean
    public CmmnHistoryService cmmnHistoryService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnHistoryService();
    }

    @Bean
    public CmmnManagementService cmmnManagementService(CmmnEngine cmmnEngine) {
        return cmmnEngine.getCmmnManagementService();
    }

}
