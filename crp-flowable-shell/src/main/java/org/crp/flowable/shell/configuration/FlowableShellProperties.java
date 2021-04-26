package org.crp.flowable.shell.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crp.flowable.shell")
public class FlowableShellProperties {
    /**
     * prefix for modeler app definitions rest call
     */
    private String modelerAppDefinitions;
    /**
     * suffix to export app models
     */
    private String modelerExport;
    /**
     * suffix to export app models as bar file
     */
    private String modelerExportBar;
    /**
     * rest endpoint for modeler models
     */
    private String modelerModels;
    private String modelerImport;
    private String modelerEditorModels;

    /**
     * Deployment deploy rest endpoint
     */
    private String deploymentDeploy;

    /**
     * audit trail rest endpoints
     */
    private String auditTrailTasks;

    private RuntimeProperties runtime;

    public String getModelerAppDefinitions() {
        return modelerAppDefinitions;
    }

    public void setModelerAppDefinitions(String modelerAppDefinitions) {
        this.modelerAppDefinitions = modelerAppDefinitions;
    }

    public String getModelerExport() {
        return modelerExport;
    }

    public void setModelerExport(String modelerExport) {
        this.modelerExport = modelerExport;
    }

    public String getModelerExportBar() {
        return modelerExportBar;
    }

    public void setModelerExportBar(String modelerExportBar) {
        this.modelerExportBar = modelerExportBar;
    }

    public String getModelerModels() {
        return modelerModels;
    }

    public void setModelerModels(String modelerModels) {
        this.modelerModels = modelerModels;
    }

    public String getModelerImport() {
        return modelerImport;
    }

    public void setModelerImport(String modelerImport) {
        this.modelerImport = modelerImport;
    }

    public String getModelerEditorModels() {
        return modelerEditorModels;
    }

    public void setModelerEditorModels(String modelerEditorModels) {
        this.modelerEditorModels = modelerEditorModels;
    }

    public String getDeploymentDeploy() {
        return deploymentDeploy;
    }

    public void setDeploymentDeploy(String deploymentDeploy) {
        this.deploymentDeploy = deploymentDeploy;
    }

    public String getAuditTrailTasks() {
        return auditTrailTasks;
    }

    public void setAuditTrailTasks(String auditTrailTasks) {
        this.auditTrailTasks = auditTrailTasks;
    }

    public RuntimeProperties getRuntime() {
        return runtime;
    }

    public void setRuntime(RuntimeProperties runtime) {
        this.runtime = runtime;
    }

    public static class RuntimeProperties {
        private String startProcess;
        private String queryProcesses;
        private String queryTasks;
        private String taskAction;

        public String getStartProcess() {
            return startProcess;
        }

        public void setStartProcess(String startProcess) {
            this.startProcess = startProcess;
        }

        public String getQueryProcesses() {
            return queryProcesses;
        }

        public void setQueryProcesses(String queryProcesses) {
            this.queryProcesses = queryProcesses;
        }

        public String getQueryTasks() {
            return queryTasks;
        }

        public void setQueryTasks(String queryTasks) {
            this.queryTasks = queryTasks;
        }

        public String getTaskAction() {
            return taskAction;
        }

        public void setTaskAction(String taskAction) {
            this.taskAction = taskAction;
        }
    }
}
