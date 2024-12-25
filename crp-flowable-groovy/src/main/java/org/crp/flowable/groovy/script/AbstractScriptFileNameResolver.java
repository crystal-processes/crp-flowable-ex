package org.crp.flowable.groovy.script;

import org.apache.commons.io.FileUtils;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.scripting.Resolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author martin.grofcik
 */
public abstract class AbstractScriptFileNameResolver implements Resolver {
    private static final Logger LOG = Logger.getLogger(AbstractScriptFileNameResolver.class.getTypeName());
    protected String fileName;

    protected static void handleContentDiffer(File file, String script, ScriptDiffersStrategy strategy) {
        LOG.log(Level.FINEST, "The content differ file {0} and script {2}.",
                new Object[]{file.getPath(), script});
        switch (strategy) {
            case THROW: {
                if (file.length() != 0) {
                    LOG.log(Level.SEVERE, "The content differ file {0} and script {2}. The strategy forces to throw an exception",
                            new Object[]{file.getPath(), script});
                    throw new FlowableException("The content of file " + file.getAbsolutePath() + File.separator + file.getName() + " differs from the content of script.");
                }
            }
            case OVERRIDE: {
                try (ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(script.getBytes())) {
                    LOG.log(Level.INFO, "Overriding file content {0}.", new Object[]{file.getPath()});
                    FileUtils.copyInputStreamToFile(scriptInputStream, file);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "IOException occured", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return "javax.script.filename".equals(key);
    }

    @Override
    public Object get(Object key) {
        if ("javax.script.filename".equals(key)) {
            return fileName;
        }
        return null;
    }
}
