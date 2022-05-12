package org.crp.flowable.coverage.bpmn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.agenda.DefaultFlowableEngineAgenda;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportFlowableEngineAgenda extends DefaultFlowableEngineAgenda {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportFlowableEngineAgenda.class);

    protected Map<String, ReportEvent> reportEvents;
    private String reportFileName;

    public ReportFlowableEngineAgenda(CommandContext commandContext, String reportFileName) {
        super(commandContext);
        this.reportFileName = reportFileName;
        reportEvents = new HashMap<>();
    }

    /**
     * Generic method to plan a {@link Runnable}.
     */
    @Override
    public void planOperation(Runnable operation, ExecutionEntity executionEntity) {
        ReportEvent reportEvent = createReportEvent(executionEntity, reportEvents);
        LOGGER.debug("Adding report event {}", reportEvent);

        super.planOperation(operation, executionEntity);
    }

    protected ReportEvent createReportEvent(ExecutionEntity executionEntity, Map<String, ReportEvent> reportEvents) {
        ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(executionEntity.getProcessDefinitionId());
        FlowElement currentFlowElement = executionEntity.getCurrentFlowElement();
        String reportEventKey = getReportEventKey(processDefinition.getResourceName(), processDefinition.getKey(), currentFlowElement.getId());
        if (!reportEvents.containsKey(reportEventKey)) {
            ProcessDefinitionUtil.getProcess(processDefinition.getId()).getFlowElements().forEach(
                    element -> {
                        String reportEventKey1 = getReportEventKey(processDefinition.getResourceName(), processDefinition.getKey(), element.getId());
                        reportEvents.put(reportEventKey1,
                                new ReportEvent.Builder().
                                        fileName(processDefinition.getResourceName()).processDefinitionKey(processDefinition.getKey()).
                                        flowElementId(element.getId()).lineNumber(element.getXmlRowNumber()).
                                        event("").build()
                );
                    });
        }
        reportEvents.get(reportEventKey).addHit();
        return null;
    }

    protected String getReportEventKey(String resourceName, String key, String flowElementId) {
        return resourceName+"-"+key+"-"+flowElementId;
    }

    @Override
    public void close() {
        if (!reportEvents.isEmpty()) {
            LOGGER.debug("Storing [{}] report events into file [{}]", reportEvents.size(), reportFileName);
            try {
                File reportFile = new File(reportFileName);
                if (!reportFile.exists()) {
                    FileUtils.createParentDirectories(reportFile);
                }
                try (FileWriter fileWriter = new FileWriter(reportFile, true)) {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                        reportEvents.values().forEach(
                                event -> {
                                    try {
                                        bufferedWriter.write(event.toString());
                                        bufferedWriter.newLine();
                                    } catch (IOException e) {
                                        LOGGER.error("Unable to add report event to file ["+reportFileName+"]", e);
                                        throw new RuntimeException("Unable to add report event to file ["+reportFileName+"]", e);
                                    }
                                }
                        );
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Unable to add report events to file ["+reportFileName+"]", e);
                throw new RuntimeException("Unable to add report events to file ["+reportFileName+"]", e);
            }
        }
    }
}
