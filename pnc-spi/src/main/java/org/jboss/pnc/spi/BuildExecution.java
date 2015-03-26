package org.jboss.pnc.spi;

public interface BuildExecution {

    String getTopContentId();

    String getBuildSetContentId();

    String getBuildContentId();

    String getProjectName();
}
