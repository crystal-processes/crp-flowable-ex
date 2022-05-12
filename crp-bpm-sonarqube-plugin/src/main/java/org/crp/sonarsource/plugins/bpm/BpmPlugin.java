package org.crp.sonarsource.plugins.bpm;

import org.crp.sonarsource.plugins.bpm.languages.BpmnLanguage;
import org.crp.sonarsource.plugins.bpm.languages.BpmnQualityProfile;
import org.crp.sonarsource.plugins.bpm.rules.BpmnCoverageLoaderSensor;
import org.crp.sonarsource.plugins.bpm.settings.BpmnLanguageProperties;
import org.sonar.api.Plugin;

/**
 * Business process management plugin for sonarqube
 */
public class BpmPlugin implements Plugin {

  @Override
  public void define(Context context) {
    context.addExtensions(BpmnLanguage.class, BpmnQualityProfile.class);
    context.addExtensions(BpmnLanguageProperties.getProperties());

    context
            .addExtension(BpmnCoverageLoaderSensor.class);
  }
}
