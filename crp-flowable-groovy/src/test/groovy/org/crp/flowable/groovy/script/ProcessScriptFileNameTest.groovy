package org.crp.flowable.groovy.script

import org.apache.commons.io.FileUtils
import org.flowable.common.engine.api.FlowableException
import org.flowable.engine.ProcessEngineConfiguration
import org.flowable.engine.RuntimeService
import org.flowable.engine.test.Deployment
import org.flowable.engine.test.FlowableTest
import org.junit.jupiter.api.Test

import java.nio.charset.Charset

import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.crp.flowable.assertions.CrpFlowableAssertions.assertThat

/**
 * @author martin.grofcik
 */
@FlowableTest
class ProcessScriptFileNameTest {

    @Test
    @Deployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.bpmn20.xml')
    void runScript(RuntimeService runtimeService) {
        def scriptProcess = runtimeService.createProcessInstanceBuilder().processDefinitionKey('oneScriptTask').start()

        assertThat(scriptProcess)
                .inHistory()
                .hasVariableWithValue('className', 'GetClassNameScriptTask')
        assertThat(new File('src/test/groovy/bpmn/oneScriptTask/GetClassNameScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("""

        this.getClass().getTypeName()

        """)
    }

    @Test
    @Deployment(resources= 'org/crp/flowable/groovy/script/oneScriptTaskWithPackage.bpmn20.xml')
    void runScriptWithPackage(RuntimeService runtimeService) {
        def scriptProcess = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey('oneScriptTaskWithPackage').start()

        assertThat(scriptProcess)
                .inHistory()
                .as('Package name depends on the package in the script not on the fileName.')
                .hasVariableWithValue('className', 'oneScriptTask.GetClassNameScriptTask')
    }

    @Test
    @Deployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.bpmn20.xml')
    void runScriptWithOverrideStrategy(RuntimeService runtimeService) {
        runScript(runtimeService)

        changeGroovyFileContent()

        def scriptProcess = runtimeService.createProcessInstanceBuilder().processDefinitionKey('oneScriptTask').start()
        assertThat(scriptProcess)
                .inHistory()
                .hasVariableWithValue('className', 'GetClassNameScriptTask')
        assertThat(new File("src/test/groovy/bpmn/oneScriptTask/GetClassNameScriptTask.groovy"))
                .content(Charset.defaultCharset()).isEqualTo("""

        this.getClass().getTypeName()

        """)
    }

    @Test
    @Deployment(resources= 'org/crp/flowable/groovy/script/oneScriptTask.bpmn20.xml')
    void runScriptWithThrowStrategy(ProcessEngineConfiguration configuration, RuntimeService runtimeService) {
        changeGroovyFileContent()
        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.THROW)

        assertThatThrownBy {
            runtimeService.createProcessInstanceBuilder().processDefinitionKey('oneScriptTask').start()
        }
                .isInstanceOf(FlowableException.class)
                .hasMessageStartingWith('The content of file ')
                .hasMessageEndingWith('GetClassNameScriptTask.groovy differs from the content of script.')

        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.OVERRIDE)
    }

    @Test
    @Deployment(resources= "org/crp/flowable/groovy/script/oneScriptTask.bpmn20.xml")
    void runScriptWithNoneStrategy(ProcessEngineConfiguration configuration, RuntimeService runtimeService) {
        changeGroovyFileContent()
        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.NONE)

        def scriptProcessInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey('oneScriptTask').start()

        assertThat(scriptProcessInstance)
                .inHistory()
                .hasVariableWithValue('className', 'GetClassNameScriptTask')
        assertThat(new File('src/test/groovy/bpmn/oneScriptTask/GetClassNameScriptTask.groovy'))
                .content(Charset.defaultCharset()).isEqualTo("NON VALID CONTENT")

        setScriptDiffersStrategyTo(configuration, ScriptDiffersStrategy.OVERRIDE)
    }

    @Test
    @Deployment(resources= 'org/crp/flowable/groovy/script/oneJuelScriptTask.bpmn20.xml')
    void runJuelScript(RuntimeService runtimeService) {
        def scriptProcess = runtimeService.createProcessInstanceBuilder().processDefinitionKey('oneScriptTask').start()

        assertThat(scriptProcess)
                .inHistory()
                .hasVariableWithValue('className', 2L)
        assertThat(new File('src/test/groovy/bpmn/oneScriptTask/GetClassNameScriptTask.juel'))
                .content(Charset.defaultCharset()).isEqualTo('${1+1}')
    }

    private static void setScriptDiffersStrategyTo(ProcessEngineConfiguration configuration, ScriptDiffersStrategy strategy) {
        def factories = configuration.scriptBindingsFactory.resolverFactories
        factories[factories.size() - 1] = new ProcessScriptFileNameResolverFactory(
                "src${File.separator}test${File.separator}groovy${File.separator}bpmn${File.separator}",
                strategy)
    }

    private static void changeGroovyFileContent() {
        def scriptFile = new File('src/test/groovy/bpmn/oneScriptTask/GetClassNameScriptTask.groovy')
        FileUtils.touch(scriptFile)
        try (ByteArrayInputStream stream = new ByteArrayInputStream('NON VALID CONTENT'.getBytes())) {
            FileUtils.copyToFile(stream, scriptFile)
        }
    }

}
