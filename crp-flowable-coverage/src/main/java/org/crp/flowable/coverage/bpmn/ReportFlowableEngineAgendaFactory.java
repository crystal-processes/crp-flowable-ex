package org.crp.flowable.coverage.bpmn;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.FlowableEngineAgendaFactory;

public class ReportFlowableEngineAgendaFactory implements FlowableEngineAgendaFactory {

    private String reportFileName;

    public ReportFlowableEngineAgendaFactory(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    @Override
    public FlowableEngineAgenda createAgenda(CommandContext commandContext) {
        return new ReportFlowableEngineAgenda(commandContext, reportFileName);
    }

}
