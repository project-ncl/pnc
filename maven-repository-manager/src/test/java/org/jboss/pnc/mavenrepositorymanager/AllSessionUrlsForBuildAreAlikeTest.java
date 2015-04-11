package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

public class AllSessionUrlsForBuildAreAlikeTest 
    extends AbstractRepositoryManagerDriverTest
{

    @Test
    public void formatRepositoryURLForSimpleInfo_AllURLsMatch() throws Exception {
        BuildExecution execution = new TestBuildExecution();

        RepositorySession repositoryConfiguration = driver.createBuildRepository(execution);
        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrl = connectionInfo.getDependencyUrl();

        assertThat(connectionInfo.getToolchainUrl(), equalTo(expectedUrl));
        assertThat(connectionInfo.getDeployUrl(), equalTo(expectedUrl));
    }

}
