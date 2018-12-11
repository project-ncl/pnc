/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.indyrepositorymanager;

import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(ContainerTest.class)
public class DependencyUrlIncludesTrackingIdAndGeneratedBuildGroupNameTest
    extends AbstractRepositoryManagerDriverTest
{

    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();

        RepositorySession repositoryConfiguration = driver.createBuildRepository(execution, accessToken, accessToken,
                RepositoryType.MAVEN);

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
