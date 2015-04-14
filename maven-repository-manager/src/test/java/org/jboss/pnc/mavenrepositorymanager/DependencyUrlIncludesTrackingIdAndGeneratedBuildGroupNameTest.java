package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

public class DependencyUrlIncludesTrackingIdAndGeneratedBuildGroupNameTest 
    extends AbstractRepositoryManagerDriverTest
{

    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();

        RepositorySession repositoryConfiguration = driver.createBuildRepository(execution);

        assertThat(repositoryConfiguration, notNullValue());

        // verify the URLs in the connection info reference this build, and refer to a tracked repository group URL
        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrlPrefix = String.format("%sfolo/track/%s", url, execution.getBuildContentId());
        String expectedGroupPathPrefix = String.format("/group/%s", execution.getBuildContentId());

        assertThat("Expected URL prefix: " + expectedUrlPrefix + "\nActual URL was: " + connectionInfo.getDependencyUrl(),
                connectionInfo.getDependencyUrl().startsWith(expectedUrlPrefix), equalTo(true));

        assertThat("Expected URL to contain group path prefix: " + expectedGroupPathPrefix + "\nActual URL was: "
                + connectionInfo.getDependencyUrl(), connectionInfo.getDependencyUrl().contains(expectedGroupPathPrefix),
                equalTo(true));
    }
}
