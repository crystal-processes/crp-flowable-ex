package org.crp.flowable.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Flowable MCP (Message Correlation Protocol) server.
 * Controls server behavior, database table prefixes, and mapping configurations.
 */
@ConfigurationProperties(prefix = "crp.flowable.mcp")
public class FlowableMcpProperties {
    
    private boolean enabled=true;
    private String datatablePrefix ="";
    private String mappingConfig = "org/crp/flowable/mcp/mapping/mappings.xml";
    
    /**
     * Gets whether the crp flowable MCP tolls are enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Gets the database table prefix for Flowable tables.
     * 
     * @return the table prefix string
     */
    public String getDatatablePrefix() {
        return datatablePrefix;
    }
    
    public void setDatatablePrefix(String datatablePrefix) {
        this.datatablePrefix = datatablePrefix;
    }
    
    /**
     * Gets the path to the MyBatis mapping configuration file.
     * 
     * @return the mapping configuration file path
     */
    public String getMappingConfig() {
        return mappingConfig;
    }
    
    public void setMappingConfig(String mappingConfig) {
        this.mappingConfig = mappingConfig;
    }
}