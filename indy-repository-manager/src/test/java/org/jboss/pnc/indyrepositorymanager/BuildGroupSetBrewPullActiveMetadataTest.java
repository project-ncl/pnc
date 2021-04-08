package org.jboss.pnc.indyrepositorymanager;

/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@Category(ContainerTest.class)
public class BuildGroupSetBrewPullActiveMetadataTest extends AbstractRepositoryManagerDriverTest {

    @Test
    public void verifyGroupHasBrewPullActiveMetadataSetWhenTrue() throws Exception {
        verifyBrewPullActiveMetadataSetProperly(true, "build_myproject_420");
    }

    @Test
    public void verifyGroupHasBrewPullActiveMetadataSetWhenFalse() throws Exception {
        verifyBrewPullActiveMetadataSetProperly(false, "build_myproject_421");
    }

    private void verifyBrewPullActiveMetadataSetProperly(boolean brewPullActive, String buildId)
            throws RepositoryManagerException, org.commonjava.indy.client.core.IndyClientException {
        // create a dummy composed (chained) build execution and a repo session based on it
        BuildExecution execution = new TestBuildExecution(buildId);
        Indy indy = driver.getIndy(accessToken);

        RepositorySession repositoryConfiguration = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap(),
                brewPullActive);

        String repoId = repositoryConfiguration.getBuildRepositoryId();
        Group buildGroup = indy.stores().load(new StoreKey(MAVEN_PKG_KEY, StoreType.group, repoId), Group.class);
        Map<String, String> metadata = buildGroup.getMetadata();

        assertThat(metadata).containsEntry(
                AbstractRepositoryManagerDriverTest.BREW_PULL_ACTIVE_METADATA_KEY,
                Boolean.toString(brewPullActive));
    }
}
