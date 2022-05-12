package org.crp.sonarsource.plugins.bpm.languages;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

/**
 * Default, BuiltIn Quality Profile for the projects having bpmn files
 */
public final class BpmnQualityProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("BPMN Rules", BpmnLanguage.KEY);
    profile.setDefault(true);

    profile.done();
  }

}
