package org.crp.sonarsource.plugins.bpm.languages;

public class ReportEvent {
    protected String fileName;
    protected String processDefinitionKey;
    protected String flowElementId;
    protected int lineNumber;
    protected int hits;
    protected String event;

    private ReportEvent(String fileName, String processDefinitionKey, String flowElementId, int lineNumber, int hits, String event) {
        this.fileName = fileName;
        this.processDefinitionKey = processDefinitionKey;
        this.flowElementId = flowElementId;
        this.lineNumber = lineNumber;
        this.hits = hits;
        this.event = event;
    }

    public String getFileName() {
        return fileName;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getFlowElementId() {
        return flowElementId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getEvent() {
        return event;
    }

    public int getHits() {
        return hits;
    }

    @Override
    public String toString() {
        return fileName + '|' +
                processDefinitionKey + '|' +
                flowElementId + '|' +
                lineNumber + '|' +
                hits + '|' +
                event + '|';
    }

    public void addHit() {
        hits++;
    }

    public void addHits(int hits) {
        this.hits += hits;
    }

    public static class Builder {
        protected String fileName;
        protected String processDefinitionKey;
        protected String flowElementId;
        protected int lineNumber = -1;
        protected int hits = 0;
        protected String event;

        public Builder() {
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder flowElementId(String flowElementId) {
            this.flowElementId = flowElementId;
            return this;
        }

        public Builder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Builder event(String event) {
            this.event = event;
            return this;
        }

        public ReportEvent build() {
            return new ReportEvent(fileName, processDefinitionKey, flowElementId, lineNumber, hits, event);
        }
        public static ReportEvent build(String eventCSV) {
            String[] eventCSVArray = eventCSV.split("\\|");
            return new ReportEvent(eventCSVArray[0], eventCSVArray[1], eventCSVArray[2], Integer.parseInt(eventCSVArray[3]), Integer.parseInt(eventCSVArray[4]), "");
        }
    }
}
