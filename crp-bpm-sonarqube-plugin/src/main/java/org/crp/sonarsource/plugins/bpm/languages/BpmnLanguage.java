package org.crp.sonarsource.plugins.bpm.languages;

import org.crp.sonarsource.plugins.bpm.settings.BpmnLanguageProperties;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines the bpmn language.
 */
public final class BpmnLanguage extends AbstractLanguage {

  public static final String NAME = "BPMN";
  public static final String KEY = "bpmn";

  private final Configuration config;

  public BpmnLanguage(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  @Override
  public String[] getFileSuffixes() {
    return config.getStringArray(BpmnLanguageProperties.FILE_SUFFIXES_KEY);
  }

}
