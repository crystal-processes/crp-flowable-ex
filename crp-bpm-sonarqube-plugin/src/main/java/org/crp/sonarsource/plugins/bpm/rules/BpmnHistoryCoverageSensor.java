package org.crp.sonarsource.plugins.bpm.rules;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
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

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a BPM coverage.
 * Results are provided as a xml file and are corresponding to the rules defined in 'rules.xml'.
 */
public class BpmnHistoryCoverageSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(BpmnHistoryCoverageSensor.class);

    protected final Configuration config;
    protected final FileSystem fileSystem;
    protected final SqlSessionFactory sessionFactory;
    protected SensorContext context;

    public BpmnHistoryCoverageSensor(final Configuration config, final FileSystem fileSystem) {
        this.config = config;
        this.fileSystem = fileSystem;
        
        // Initialize SqlSessionFactory from configuration
        String jdbcUrl = config.get("sonar.jdbc.url").orElse(null);
        String jdbcUsername = config.get("sonar.jdbc.username").orElse(null);
        String jdbcPassword = config.get("sonar.jdbc.password").orElse(null);
        String jdbcDriver = config.get("sonar.jdbc.driverClass").orElse("org.h2.Driver");
        
        if (jdbcUrl != null && jdbcUsername != null && jdbcPassword != null) {
            try {
                // Create a simple DataSource wrapper for MyBatis
                DataSource dataSource = new SonarQubeDataSource(jdbcUrl, jdbcUsername, jdbcPassword, jdbcDriver);
                
                // Build MyBatis configuration
                org.apache.ibatis.session.Configuration mybatisConfig = new org.apache.ibatis.session.Configuration();
                Environment environment = new Environment("sonar", new JdbcTransactionFactory(), dataSource);
                mybatisConfig.setEnvironment(environment);
                
                // Build SqlSessionFactory
                sessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig);
            } catch (Exception e) {
                LOGGER.error("Failed to initialize SqlSessionFactory", e);
                throw new IllegalStateException("Failed to initialize SqlSessionFactory", e);
            }
        } else {
            LOGGER.warn("Database configuration not found in config. SqlSessionFactory will be null.");
            sessionFactory = null;
        }
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

    /**
     * Simple DataSource implementation for SonarQube database configuration.
     */
    private static class SonarQubeDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        private final String driverClassName;

        SonarQubeDataSource(String url, String username, String password, String driverClassName) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }

        @Override
        public java.sql.Connection getConnection() throws SQLException {
            try {
                Class.forName(driverClassName);
                return java.sql.DriverManager.getConnection(url, username, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver class not found: " + driverClassName, e);
            }
        }

        @Override
        public java.sql.Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not supported");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public java.io.PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }

}
