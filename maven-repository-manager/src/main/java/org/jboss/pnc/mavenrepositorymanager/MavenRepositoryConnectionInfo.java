package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;

import java.util.HashMap;
import java.util.Map;

public class MavenRepositoryConnectionInfo implements RepositoryConnectionInfo {

    private static final String ALT_DEPLOY_OPTION = "altDeploymentRepository";
    private static final String ALT_DEPLOY_FORMAT = "deploy::default::%s";

    private String url;

    public MavenRepositoryConnectionInfo(String url) {
        this.url = url;
    }

    @Override
    public String getDependencyUrl() {
        return url;
    }

    @Override
    public String getToolchainUrl() {
        return url;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(ALT_DEPLOY_OPTION, String.format(ALT_DEPLOY_FORMAT, url));

        return props;
    }

    @Override
    public String getDeployUrl() {
        return url;
    }

}
