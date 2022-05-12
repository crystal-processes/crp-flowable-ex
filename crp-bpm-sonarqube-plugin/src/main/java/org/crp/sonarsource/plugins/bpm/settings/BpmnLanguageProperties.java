package org.crp.sonarsource.plugins.bpm.settings;

import java.util.List;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import static java.util.Arrays.asList;
import static org.crp.sonarsource.plugins.bpm.rules.BpmnCoverageLoaderSensor.REPORT_PATH_KEY;

public class BpmnLanguageProperties {

    public static final String FILE_SUFFIXES_KEY = "sonar.bpmn.file.suffixes";
    public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".bpmn";

    private BpmnLanguageProperties() {
    }

    public static List<PropertyDefinition> getProperties() {
        return asList(PropertyDefinition.builder(FILE_SUFFIXES_KEY)
                        .multiValues(true)
                        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
                        .category("BPMN")
                        .name("File Suffixes")
                        .description("List of suffixes for BPMN files to analyze.")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),
                PropertyDefinition.builder(REPORT_PATH_KEY)
                        .multiValues(false)
                        .defaultValue("target/bpmn-coverage-report.csv")
                        .category("BPMN")
                        .name("BPMN coverage report")
                        .description("BPMN coverage report file path.")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build()
        );
    }

}
