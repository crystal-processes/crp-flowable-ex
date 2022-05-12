package org.crp.flowable.coverage.bpmn;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crp.flowable.coverage")
public class BpmnCoverageProperties {
    // is coverage scan enabled
    private boolean enabled;
    // where to store coverage data
    private String reportPath = "target/bpmn-coverage-report.csv";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }
}
