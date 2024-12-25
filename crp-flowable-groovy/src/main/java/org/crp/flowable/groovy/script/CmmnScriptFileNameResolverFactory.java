package org.crp.flowable.groovy.script;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.cmmn.engine.impl.persistence.entity.PlanItemInstanceEntity;
import org.flowable.cmmn.model.PlanItemDefinition;
import org.flowable.cmmn.model.ScriptServiceTask;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.scripting.Resolver;
import org.flowable.common.engine.impl.scripting.ResolverFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author martin.grofcik
 */
public class CmmnScriptFileNameResolverFactory implements ResolverFactory {

    private final String pathPrefix;
    private final ScriptDiffersStrategy scriptDiffersStrategy;

    public CmmnScriptFileNameResolverFactory() {
        this("src"+File.separator+"test"+File.separator+"groovy"+File.separator+"cmmn"+File.separator, ScriptDiffersStrategy.OVERRIDE);
    }

    public CmmnScriptFileNameResolverFactory(String pathPrefix, ScriptDiffersStrategy scriptDiffersStrategy) {
        this.pathPrefix = pathPrefix;
        this.scriptDiffersStrategy = scriptDiffersStrategy;
    }

    @Override
    public Resolver createResolver(AbstractEngineConfiguration engineConfiguration, VariableContainer variableContainer) {
        return new CmmnScriptFileNameResolver(variableContainer);
    }


    private class CmmnScriptFileNameResolver extends AbstractScriptFileNameResolver {
        public CmmnScriptFileNameResolver(VariableContainer variableContainer) {
            fileName = checkFile(variableContainer,
                    CmmnScriptFileNameResolverFactory.this.pathPrefix,
                    CmmnScriptFileNameResolverFactory.this.scriptDiffersStrategy);
        }


        protected String checkFile(VariableContainer variableContainer, String prefixPath, ScriptDiffersStrategy scriptDiffersStrategy) {
            if (variableContainer instanceof PlanItemInstanceEntity planItemInstance) {
                String fileName = getFileName(planItemInstance);
                File file = new File(prefixPath + fileName);
                try {
                    FileUtils.touch(file);
                    String script = ((ScriptServiceTask) planItemInstance.getPlanItem().getPlanItemDefinition()).getScript();
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

        protected String getFileName(PlanItemInstanceEntity planItemInstance) {
            String fileName = "";
            if (StringUtils.isNotEmpty(planItemInstance.getTenantId())) {
                fileName = planItemInstance.getTenantId() + File.separator;
            }

            PlanItemDefinition planItemDefinition = planItemInstance.getPlanItem().getPlanItemDefinition();
            StringBuilder className = new StringBuilder(planItemDefinition.getId());
            while(planItemDefinition.getParent() != null) {
                planItemDefinition = planItemDefinition.getParent();
                className.insert(0, planItemDefinition.getId() + File.separator);
            }
            return fileName + className +  getExtension(planItemInstance);
        }

        private static String getExtension(PlanItemInstanceEntity planItemInstance) {
            String extension = ".unrecognized";
            if (planItemInstance.getPlanItem().getPlanItemDefinition() instanceof ScriptServiceTask scriptTask) {
                extension = "." + scriptTask.getScriptFormat();
            }
            return extension;
        }

    }
}
