package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.junit.Test;

public class RepositoryManagerDriver01Test 
    extends AbstractRepositoryManagerDriverTest
{

    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

        BuildRecordSet bc = new BuildRecordSet();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration repositoryConfiguration = driver.createRepository(pbc, bc);

        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrlPrefix = String.format("%sfolo/track/build+%s", url, pbc.getProject().getName());
        String expectedGroupPathPrefix = String.format("/group/build+%s", pbc.getProject().getName());

        assertThat("Expected URL prefix: " + expectedUrlPrefix + "\nActual URL was: " + connectionInfo.getDependencyUrl(),
                connectionInfo.getDependencyUrl().startsWith(expectedUrlPrefix), equalTo(true));

        assertThat("Expected URL to contain group path prefix: " + expectedGroupPathPrefix + "\nActual URL was: "
                + connectionInfo.getDependencyUrl(), connectionInfo.getDependencyUrl().contains(expectedGroupPathPrefix),
                equalTo(true));
    }
}
