package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {


    @Override
    public RepositoryConfiguration createRepository(BuildConfiguration buildConfiguration, BuildCollection buildCollection) throws RepositoryManagerException {

        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration() {
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
        };

        return repositoryConfiguration;
    }

    @Override
    public void persistArtifacts(RepositoryConfiguration repository, BuildRecord buildRecord) {
    }


    @Override
    public boolean canManage(RepositoryType managerType) {
        return true;
    }

}
