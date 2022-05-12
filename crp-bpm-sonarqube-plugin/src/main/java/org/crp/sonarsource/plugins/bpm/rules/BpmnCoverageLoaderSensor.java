package org.crp.sonarsource.plugins.bpm.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.crp.sonarsource.plugins.bpm.languages.BpmnLanguage;
import org.crp.sonarsource.plugins.bpm.languages.ReportEvent;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a fictive external tool named: FooLint
 * Results are provided as an xml file and are corresponding to the rules defined in 'rules.xml'.
 * To be very abstract, these rules are applied on source files made with the fictive language Foo.
 */
public class BpmnCoverageLoaderSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(BpmnCoverageLoaderSensor.class);

    public static final String REPORT_PATH_KEY = "crp.flowable.coverage.reportPath";

    protected final Configuration config;
    protected final FileSystem fileSystem;
    protected SensorContext context;

    public BpmnCoverageLoaderSensor(final Configuration config, final FileSystem fileSystem) {
        this.config = config;
        this.fileSystem = fileSystem;
    }

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name("Bpmn Coverage Loader Sensor");
        descriptor.onlyOnLanguage(BpmnLanguage.KEY);
    }

    protected String reportPathKey() {
        return REPORT_PATH_KEY;
    }

    protected String getReportPath() {
        return config.get(reportPathKey()).orElse(null);
    }

    @Override
    public void execute(final SensorContext context) {
        String reportPath = getReportPath();
        if (reportPath != null) {
            this.context = context;
            File reportResultsFile = new File(reportPath);
            try {
                parseReportAndAddCoverage(reportResultsFile);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("Unable to parse the provided "+reportPath+" file", e);
            }
        }
    }

    protected void parseReportAndAddCoverage(final File file) throws FileNotFoundException {
        LOGGER.info("Parsing {} test coverage results", file.getName());
        Map<String, InputFile> bpmnFileMap = StreamSupport.stream(fileSystem.inputFiles(fileSystem.predicates().hasLanguage(BpmnLanguage.KEY)).spliterator(), false)
                .collect(
                        Collectors.toMap(InputFile::filename, inputFile -> inputFile)
                );
        Map<String, Map<String, ReportEvent>> report = parseCoverageReport(file);

        bpmnFileMap.forEach((bpmnFileName, inputFile) -> {
            LOGGER.debug("Adding coverage to file {}", bpmnFileName);
            Map<String, ReportEvent> reportEvents = getReportEventsForFile(report, bpmnFileName);
            Optional<String> convertedInputFileName = bpmnFileMap.keySet().stream().filter(bpmnFileName::endsWith).findFirst();
            if (convertedInputFileName.isPresent()) {
                NewCoverage coverage = context.newCoverage().onFile(inputFile);
                reportEvents.values().forEach(reportEvent ->
                        coverage.lineHits(reportEvent.getLineNumber(), reportEvent.getHits())
                );
                coverage.save();
            } else {
                LOGGER.warn("Unable to find file {} in the report events file", file);
            }
        });
        LOGGER.info("Parsing {} test coverage results - done ", file.getName());
    }

    protected Map<String, ReportEvent> getReportEventsForFile(Map<String, Map<String, ReportEvent>> report, String bpmnFileName) {
        for (String fileName : report.keySet()) {
            if (fileName.endsWith(bpmnFileName)) {
                return report.get(fileName);
            }
        }
        return Collections.emptyMap();
    }

    protected Map<String, Map<String, ReportEvent>> parseCoverageReport(File file) throws FileNotFoundException {
        Map<String, Map<String, ReportEvent>> coverageReport = new HashMap<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                addEventToCoverageReport(coverageReport, scanner.nextLine());
            }
        }
        return coverageReport;
    }

    protected void addEventToCoverageReport(Map<String, Map<String, ReportEvent>> coverageReport, String eventLine) {
        ReportEvent reportEvent = ReportEvent.Builder.build(eventLine);
        if (!coverageReport.containsKey(reportEvent.getFileName())) {
            coverageReport.put(reportEvent.getFileName(), new HashMap<>());
        }

        Map<String, ReportEvent> fileEvents = coverageReport.get(reportEvent.getFileName());
        if (fileEvents.containsKey(reportEvent.getFlowElementId())) {
            fileEvents.get(reportEvent.getFlowElementId()).addHits(reportEvent.getHits());
        } else {
            fileEvents.put(reportEvent.getFlowElementId(), reportEvent);
        }
    }

    @Override
    public String toString() {
        return "Bpmn coverage loader sensor";
    }

}
