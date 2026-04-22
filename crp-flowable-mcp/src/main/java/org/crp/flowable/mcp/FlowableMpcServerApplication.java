package org.crp.flowable.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Flowable MCP (Message Correlation Protocol) Server.
 * This Spring Boot application provides developer tools for analyzing and troubleshooting
 * Flowable business processes.
 */
@SpringBootApplication
public class FlowableMpcServerApplication {

    /**
     * Main entry point for the Flowable MCP Server application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FlowableMpcServerApplication.class, args);
    }
}
