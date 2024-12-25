package org.crp.flowable.groovy.script

import org.apache.commons.io.FileUtils
import org.flowable.cmmn.api.CmmnRuntimeService
import org.flowable.cmmn.engine.CmmnEngineConfiguration
import org.flowable.cmmn.engine.test.CmmnDeployment
import org.flowable.cmmn.engine.test.FlowableCmmnTest
import org.flowable.common.engine.api.FlowableException
import org.junit.jupiter.api.Test

import java.nio.charset.Charset

import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat

/**
 * @author martin.grofcik
 */
@FlowableCmmnTest
class CmmnScriptFileNameTest {

    @Test
    @CmmnDeployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.cmmn')
    void runScript(CmmnRuntimeService cmmnRuntimeService) {
        def scriptCase = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey('scriptCase').start()

        assertThat(scriptCase.getCaseVariables()).containsEntry('className', 'ScriptTask')
        assertThat(new File('src/test/groovy/cmmn/myScriptPlanModel/ScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("this.getClass().getTypeName()")
    }

    @Test
    @CmmnDeployment(resources= 'org/crp/flowable/groovy/script/oneScriptTaskWithPackage.cmmn')
    void runScriptWithPackage(CmmnRuntimeService cmmnRuntimeService) {
        def scriptCase =  cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey('scriptCase').start()

        assertThat(scriptCase.getCaseVariables()).containsEntry('className', 'oneScriptTask.ScriptTask')
        assertThat(new File('src/test/groovy/cmmn/myScriptPlanModelWithPackage/ScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("""package oneScriptTask
            this.getClass().getTypeName()""")
    }

    @Test
    @CmmnDeployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.cmmn')
    void runScriptWithOverrideStrategy(CmmnRuntimeService cmmnRuntimeService) {
        changeGroovyFileContent()

        def scriptCase =  cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey('scriptCase').start()
        assertThat(scriptCase.getCaseVariables()).containsEntry('className', 'ScriptTask')
        assertThat(new File('src/test/groovy/cmmn/myScriptPlanModelWithPackage/ScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("""package oneScriptTask
            this.getClass().getTypeName()""")
    }

    @Test
    @CmmnDeployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.cmmn')
    void runScriptWithThrowStrategy(CmmnEngineConfiguration configuration, CmmnRuntimeService cmmnRuntimeService) {
        changeGroovyFileContent()
        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.THROW)

        assertThatThrownBy {
            cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey('scriptCase').start()
        }
                .isInstanceOf(FlowableException.class)
                .hasMessageStartingWith('The content of file ')
                .hasMessageEndingWith('ScriptTask.groovy differs from the content of script.')

        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.OVERRIDE)
    }

    @Test
    @CmmnDeployment(resources= "org/crp/flowable/groovy/script/oneScriptTask.cmmn")
    void runScriptWithNoneStrategy(CmmnEngineConfiguration configuration, CmmnRuntimeService cmmnRuntimeService) {
        changeGroovyFileContent()
        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.NONE)

        def scriptCase =  cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey('scriptCase').start()
        assertThat(scriptCase.getCaseVariables()).containsEntry('className', 'ScriptTask')
        assertThat(new File('src/test/groovy/cmmn/myScriptPlanModel/ScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("NON VALID CONTENT")

        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.OVERRIDE)
    }

    private static void setScriptDiffersStrategyTo(CmmnEngineConfiguration configuration, ScriptDiffersStrategy strategy) {
        def factories = configuration.scriptBindingsFactory.resolverFactories
        factories[factories.size() - 1] = new CmmnScriptFileNameResolverFactory(
                "src${File.separator}test${File.separator}groovy${File.separator}cmmn${File.separator}",
                strategy)
    }

    private static void changeGroovyFileContent() {
        def scriptFile = new File('src/test/groovy/cmmn/myScriptPlanModel/ScriptTask.groovy')
        FileUtils.touch(scriptFile)
        try (ByteArrayInputStream stream = new ByteArrayInputStream('NON VALID CONTENT'.getBytes())) {
            FileUtils.copyToFile(stream, scriptFile)
        }
    }

}
