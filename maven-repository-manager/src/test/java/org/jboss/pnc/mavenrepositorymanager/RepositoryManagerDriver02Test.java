package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.junit.Test;

public class RepositoryManagerDriver02Test 
    extends AbstractRepositoryManagerDriverTest
{

    @Test
    public void formatRepositoryURLForSimpleInfo_AllURLsMatch() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

        BuildRecordSet bc = new BuildRecordSet();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration repositoryConfiguration = driver.createRepository(pbc, bc);
        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrl = connectionInfo.getDependencyUrl();

        assertThat(connectionInfo.getToolchainUrl(), equalTo(expectedUrl));
        assertThat(connectionInfo.getDeployUrl(), equalTo(expectedUrl));
    }

}
