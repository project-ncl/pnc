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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.apache.commons.lang.StringUtils.join;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(ContainerTest.class)
public class BuildGroupIncludesProductVersionGroupTest extends AbstractRepositoryManagerDriverTest {

    @Test
    public void verifyGroupComposition_ProductVersion_NoConfSet() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution("build_myproject_12345");
        Indy indy = driver.getIndy(accessToken);

        RepositorySession repositoryConfiguration = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap(),
                false);
        String repoId = repositoryConfiguration.getBuildRepositoryId();

        assertThat(repoId, equalTo(execution.getBuildContentId()));

        // check that the build group includes:
        // - the build's hosted repo
        // - the product version repo group
        // - the "shared-releases" repo group
        // - the "shared-imports" repo
        // - the public group
        // ...in that order
        Group buildGroup = indy.stores().load(new StoreKey(MAVEN_PKG_KEY, StoreType.group, repoId), Group.class);

        System.out.printf("Constituents:\n  %s\n", join(buildGroup.getConstituents(), "\n  "));
        assertGroupConstituents(
                buildGroup,
                new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, execution.getBuildContentId()),
                new StoreKey(
                        MAVEN_PKG_KEY,
                        StoreType.group,
                        IndyRepositoryConstants.COMMON_BUILD_GROUP_CONSTITUENTS_GROUP));
    }

}
