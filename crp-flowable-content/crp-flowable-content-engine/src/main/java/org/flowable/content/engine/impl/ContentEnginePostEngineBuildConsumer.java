package org.flowable.content.engine.impl;

import org.flowable.common.engine.api.engine.EngineLifecycleListener;
import org.flowable.content.engine.ContentEngine;
import org.flowable.content.engine.ContentEngineConfiguration;

import java.util.function.Consumer;

public class ContentEnginePostEngineBuildConsumer implements Consumer<ContentEngine> {
    @Override
    public void accept(ContentEngine contentEngine) {
        ContentEngineConfiguration engineConfiguration = contentEngine.getContentEngineConfiguration();
        if (engineConfiguration.getEngineLifecycleListeners() != null) {
            for (EngineLifecycleListener engineLifecycleListener : engineConfiguration.getEngineLifecycleListeners()) {
                engineLifecycleListener.onEngineBuilt(contentEngine);
            }
        }
    }
}
