package org.jboss.pnc.mavenrepositorymanager.fixture;

import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildExecutionType;

public class TestBuildExecution implements BuildExecution {

    private String topContentId;

    private String buildSetContentId;

    private String buildContentId;

    private String projectName = "my project";

    private BuildExecutionType buildExecutionType;

    public TestBuildExecution(String topId, String setId, String buildId, BuildExecutionType type) {
        this.topContentId = topId;
        this.buildSetContentId = setId;
        this.buildContentId = buildId;
        this.buildExecutionType = type;
    }

    public TestBuildExecution() {
        this("product+myproduct+1-0", null, "build+myproject+12345", BuildExecutionType.STANDALONE_BUILD);
    }

    @Override
    public String getTopContentId() {
        return topContentId;
    }

    @Override
    public String getBuildSetContentId() {
        return buildSetContentId;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    public void setTopContentId(String topContentId) {
        this.topContentId = topContentId;
    }

    public void setBuildSetContentId(String buildSetContentId) {
        this.buildSetContentId = buildSetContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public BuildExecutionType getBuildExecutionType() {
        return buildExecutionType;
    }

}
