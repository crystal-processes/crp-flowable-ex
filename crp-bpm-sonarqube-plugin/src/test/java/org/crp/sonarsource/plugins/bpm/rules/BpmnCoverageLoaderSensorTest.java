package org.crp.sonarsource.plugins.bpm.rules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.crp.sonarsource.plugins.bpm.languages.BpmnLanguage;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Configuration;

class BpmnCoverageLoaderSensorTest {

    private final Configuration configuration = mock(Configuration.class);
    private final FileSystem fileSystem = mock(FileSystem.class);

    private final BpmnCoverageLoaderSensor sensor = new BpmnCoverageLoaderSensor(configuration, fileSystem);

    @Test
    void description() {
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        sensor.describe(descriptor);
        verify(descriptor).name("Bpmn Coverage Loader Sensor");
        verify(descriptor).onlyOnLanguage(BpmnLanguage.KEY);
        reset(descriptor);
    }

    @Test
    void parse_events_and_sets_coverage() {
        SensorContext context = mock(SensorContext.class);
        FilePredicates predicates = mock(FilePredicates.class);
        when(configuration.get("crp.flowable.coverage.reportPath")).thenReturn(Optional.of("src/test/resources/eventReport.txt"));
        when(fileSystem.predicates()).thenReturn(predicates);
        InputFile inputBpmnFile = mock(InputFile.class);
        when(inputBpmnFile.filename()).thenReturn("org/crp/flowable/test/oneTaskProcess.bpmn20.xml");
        NewCoverage newCoverage = mock(NewCoverage.class);
        when(context.newCoverage()).thenReturn(newCoverage);
        List<InputFile> files = Collections.singletonList(inputBpmnFile);
        when(fileSystem.inputFiles(any())).thenReturn(files);
        FilePredicate acceptAllPredicate = mock(FilePredicate.class);
        when(acceptAllPredicate.apply(any())).thenReturn(true);
        when(predicates.hasType(any())).thenReturn(acceptAllPredicate);
        when(predicates.and(any(), any())).thenReturn(acceptAllPredicate);
        when(newCoverage.onFile(inputBpmnFile)).thenReturn(newCoverage);

        sensor.execute(context);

        verify(newCoverage, times(1)).lineHits(7,1);

        reset(configuration, fileSystem);
    }
}