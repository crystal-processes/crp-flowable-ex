package org.crp.flowable.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crp.flowable.mcp")
public class FlowableMcpProperties {
    
    private boolean enabled=true;
    private String datatablePrefix ="";
    private String mappingConfig = "org/crp/flowable/mcp/mapping/mappings.xml";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDatatablePrefix() {
        return datatablePrefix;
    }
    
    public void setDatatablePrefix(String datatablePrefix) {
        this.datatablePrefix = datatablePrefix;
    }
    
    public String getMappingConfig() {
        return mappingConfig;
    }
    
    public void setMappingConfig(String mappingConfig) {
        this.mappingConfig = mappingConfig;
    }
}