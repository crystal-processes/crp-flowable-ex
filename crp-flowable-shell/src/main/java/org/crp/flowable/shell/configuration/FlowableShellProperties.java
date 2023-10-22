package org.crp.flowable.shell.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crp.flowable.shell")
public class FlowableShellProperties {

    private String login;
    private String password;
    private String restURL;
    private String idmURL;
    private String sourceTestJavaDir;

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

    private String historicActivityInstances;
    private String processDefinitions;

    /**
     * Deployment deploy rest endpoint
     */
    private String deploymentDeploy;

    private String designerURL;
    private String token;

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

    public String getHistoricActivityInstances() {
        return historicActivityInstances;
    }

    public void setHistoricActivityInstances(String historicActivityInstances) {
        this.historicActivityInstances = historicActivityInstances;
    }

    public String getProcessDefinitions() {
        return processDefinitions;
    }

    public void setProcessDefinitions(String processDefinitions) {
        this.processDefinitions = processDefinitions;
    }

    public String getRestURL() {
        return restURL;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getIdmURL() {
        return idmURL;
    }

    public void setIdmURL(String idmURL) {
        this.idmURL = idmURL;
    }

    public String getSourceTestJavaDir() {
        return sourceTestJavaDir;
    }

    public void setSourceTestJavaDir(String sourceTestJavaDir) {
        this.sourceTestJavaDir = sourceTestJavaDir;
    }

    public String getDesignerURL() {
        return designerURL;
    }

    public void setDesignerURL(String designerURL) {
        this.designerURL = designerURL;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
