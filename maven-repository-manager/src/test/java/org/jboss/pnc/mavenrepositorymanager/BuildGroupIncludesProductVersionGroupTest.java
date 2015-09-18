/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mavenrepositorymanager;

import static org.apache.commons.lang.StringUtils.join;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

public class BuildGroupIncludesProductVersionGroupTest extends AbstractRepositoryManagerDriverTest {

    @Test
    public void verifyGroupComposition_ProductVersion_NoConfSet() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution("product_myproduct_1.0", null, "build_myproject_12345",
                false);
        Aprox aprox = driver.getAprox();

        RepositorySession repositoryConfiguration = driver.createBuildRepository(execution);
        String repoId = repositoryConfiguration.getBuildRepositoryId();

        assertThat(repoId, equalTo(execution.getBuildContentId()));

        // check that the build group includes:
        // - the build's hosted repo
        // - the product version repo group
        // - the "shared-releases" repo group
        // - the "shared-imports" repo
        // - the public group
        // ...in that order
        Group buildGroup = aprox.stores().load(StoreType.group, repoId, Group.class);

        System.out.printf("Constituents:\n  %s\n", join(buildGroup.getConstituents(), "\n  "));
        assertGroupConstituents(buildGroup, new StoreKey(StoreType.hosted, execution.getBuildContentId()),
                new StoreKey(StoreType.group, execution.getTopContentId()), new StoreKey(StoreType.group,
                        MavenRepositoryConstants.SHARED_RELEASES_ID), new StoreKey(StoreType.hosted,
                        MavenRepositoryConstants.SHARED_IMPORTS_ID), new StoreKey(StoreType.group,
                        MavenRepositoryConstants.PUBLIC_GROUP_ID));
    }

}
