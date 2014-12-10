package org.jboss.pnc.rest.assertion;

import org.assertj.core.api.AbstractAssert;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;

public class ProjectBuildConfigurationRestAssertion extends AbstractAssert<ProjectBuildConfigurationRestAssertion, ProjectBuildConfigurationRest> {

    protected ProjectBuildConfigurationRestAssertion(ProjectBuildConfigurationRest actual) {
        super(actual, ProjectBuildConfigurationRestAssertion.class);
    }

    public static ProjectBuildConfigurationRestAssertion assertThat(ProjectBuildConfigurationRest actual) {
        return new ProjectBuildConfigurationRestAssertion(actual);
    }

    public ProjectBuildConfigurationRestAssertion hasId(Integer id) {
        if(!actual.getId().equals(id)) {
            failWithMessage("Expected id %i but was %i", actual.getId(), id);
        }
        return this;
    }

    public ProjectBuildConfigurationRestAssertion hasProjectName(String projectName) {
        if(!actual.getProjectName().equals(projectName)) {
            failWithMessage("Expected Project name %s but was %s", actual.getProjectName(), projectName);
        }
        return this;
    }

    public ProjectBuildConfigurationRestAssertion hasIdentifier(String identifier) {
        if(!actual.getIdentifier().equals(identifier)) {
            failWithMessage("Expected identifier %s but was %s", actual.getIdentifier(), identifier);
        }
        return this;
    }
}
