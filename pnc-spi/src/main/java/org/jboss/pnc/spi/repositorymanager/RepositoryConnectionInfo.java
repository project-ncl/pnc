package org.jboss.pnc.spi.repositorymanager;

import java.util.Map;

public interface RepositoryConnectionInfo
{

    String getDependencyUrl();

    String getToolchainUrl();

    String getDeployUrl();

    Map<String, String> getProperties();

}
