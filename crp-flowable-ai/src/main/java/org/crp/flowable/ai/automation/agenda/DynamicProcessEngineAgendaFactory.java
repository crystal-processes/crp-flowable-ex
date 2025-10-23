package org.crp.flowable.ai.automation.agenda;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.FlowableEngineAgendaFactory;

public class DynamicProcessEngineAgendaFactory implements FlowableEngineAgendaFactory {

    private FlowableEngineAgendaFactory parentAgendaFactory;

    public DynamicProcessEngineAgendaFactory(FlowableEngineAgendaFactory parentAgendaFactory) {
        this.parentAgendaFactory = parentAgendaFactory;
    }

    @Override
    public FlowableEngineAgenda createAgenda(CommandContext commandContext) {
        return new DynamicProcessEngineAgenda(commandContext, parentAgendaFactory.createAgenda(commandContext));
    }
}
