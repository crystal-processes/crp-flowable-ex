package org.crp.flowable.groovy.script;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.ScriptTask;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.scripting.Resolver;
import org.flowable.common.engine.impl.scripting.ResolverFactory;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author martin.grofcik
 */
public class ProcessScriptFileNameResolverFactory implements ResolverFactory {

    private final String pathPrefix;
    private final ScriptDiffersStrategy scriptDiffersStrategy;

    public ProcessScriptFileNameResolverFactory() {
        this("src"+File.separator+"test"+File.separator+"groovy"+File.separator+"bpmn"+File.separator,
                ScriptDiffersStrategy.OVERRIDE);
    }

    /**
     * Provides a resolver for the script file name for jsr223-compliant script engines.
     *
     * @param pathPrefix the prefix path where to store file content if scriptDiffersStrategy allows file content overwriting
     * @param scriptDiffersStrategy defines what to do if file and script content differ.
     */
    public ProcessScriptFileNameResolverFactory(String pathPrefix, ScriptDiffersStrategy scriptDiffersStrategy) {
        this.pathPrefix = pathPrefix;
        this.scriptDiffersStrategy = scriptDiffersStrategy;
    }

    @Override
    public Resolver createResolver(AbstractEngineConfiguration engineConfiguration, VariableContainer variableContainer) {
        return new ProcessScriptFileNameResolver(variableContainer);
    }


    class ProcessScriptFileNameResolver extends AbstractScriptFileNameResolver {

        public ProcessScriptFileNameResolver(VariableContainer variableContainer) {
            this.fileName = checkFile(variableContainer,
                    ProcessScriptFileNameResolverFactory.this.pathPrefix,
                    ProcessScriptFileNameResolverFactory.this.scriptDiffersStrategy);
        }

        protected static String checkFile(VariableContainer variableContainer, String prefixPath, ScriptDiffersStrategy scriptDiffersStrategy) {
            if (variableContainer instanceof ExecutionEntity execution) {
                String fileName = getFileName(execution);
                File file = new File(prefixPath + fileName);
                try {
                    FileUtils.touch(file);
                    String script = ((ScriptTask) execution.getCurrentFlowElement()).getScript();
                    try (ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(script.getBytes())) {
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            if (!IOUtils.contentEquals(scriptInputStream, fileInputStream)) {
                                handleContentDiffer(file, script, scriptDiffersStrategy);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return fileName;
            }
            return null;
        }

        protected static String getFileName(ExecutionEntity execution) {
            String fileName = "";
            if (StringUtils.isNotEmpty(execution.getTenantId())) {
                fileName = execution.getTenantId() + File.separator;
            }
            return fileName + execution.getProcessDefinitionKey() + File.separator + execution.getCurrentActivityId() + getExtension(execution);
        }

        private static String getExtension(ExecutionEntity execution) {
            String extension = ".unrecognized";
            if (execution.getCurrentFlowElement() instanceof ScriptTask scriptTask) {
                extension = "." + scriptTask.getScriptFormat();
            }
            return extension;
        }
    }
}
