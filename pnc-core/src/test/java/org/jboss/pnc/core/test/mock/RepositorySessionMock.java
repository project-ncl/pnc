package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public class RepositorySessionMock implements RepositorySession {
    @Override
    public RepositoryType getType() {
        return RepositoryType.MAVEN;
    }

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public String getCollectionId() {
        return "test-collection";
    }

    @Override
    public RepositoryConnectionInfo getConnectionInfo() {
        return new RepositoryConnectionInfo() {
            // TODO: This is not connected to anything...
            String repo = "http://localhost:8090/api/groups/test";

            @Override
            public String getToolchainUrl() {
                return repo;
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> props = new HashMap<String, String>();
                props.put("altDeploymentRepository", "test::default::" + repo);

                return props;
            }

            @Override
            public String getDependencyUrl() {
                return repo;
            }

            @Override
            public String getDeployUrl() {
                return repo;
            }
        };
    }

    @Override
    public RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException {
        return new RepositoryManagerResult() {
            @Override
            public List<Artifact> getBuiltArtifacts() {
                List<Artifact> builtArtifacts = new ArrayList<>();
                builtArtifacts.add(getArtifact(1));
                return builtArtifacts;
            }

            @Override
            public List<Artifact> getDependencies() {
                List<Artifact> dependencies = new ArrayList<>();
                dependencies.add(getArtifact(10));
                return dependencies;
            }
        };
    }

    private Artifact getArtifact(int i) {
        Artifact artifact = new Artifact();
        artifact.setId(i);
        artifact.setIdentifier("test" + i);
        return artifact;
    }
}
