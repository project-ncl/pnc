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

import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor;
import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(ContainerTest.class)
public class ExtraDependencyRepositoriesTest extends AbstractImportTest {

    @Test
    public void shouldExtractReposFromString() {
        Map<String, String> genericParams = createGenericParamsMap(
                "http://central.maven.org/maven2/\n" + "https://maven.repository.redhat.com/ga/");

        List<ArtifactRepository> repos = driver.extractExtraRepositoriesFromGenericParameters(genericParams);
        assertEquals(2, repos.size());
        assertEquals("http://central.maven.org/maven2/", repos.get(0).getUrl());
        assertEquals("central-maven-org", repos.get(0).getId());
        assertEquals("https://maven.repository.redhat.com/ga/", repos.get(1).getUrl());
        assertEquals("maven-repository-redhat-com", repos.get(1).getId());
    }

    @Test
    public void shouldSkipMalformedURL() {
        Map<String, String> genericParams = createGenericParamsMap("http/central.maven.org/maven2/");

        List<ArtifactRepository> repos = driver.extractExtraRepositoriesFromGenericParameters(genericParams);
        assertEquals(0, repos.size());
    }

    @Test
    public void shouldAddExtraRepositoryToBuildGroup() throws RepositoryManagerException, IndyClientException {
        final String REPO_NAME = "i-test-com";
        Map<String, String> genericParams = createGenericParamsMap("http://test.com/maven/");
        BuildExecution execution = new TestBuildExecution();

        RepositorySession repositorySession = driver
                .createBuildRepository(execution, accessToken, accessToken, RepositoryType.MAVEN, genericParams);
        assertNotNull(repositorySession);

        StoreKey buildGroupKey = new StoreKey(
                MavenPackageTypeDescriptor.MAVEN_PKG_KEY,
                StoreType.group,
                repositorySession.getBuildRepositoryId());
        Group buildGroup = indy.stores().load(buildGroupKey, Group.class);

        long hits = buildGroup.getConstituents().stream().filter((key) -> REPO_NAME.equals(key.getName())).count();

        assertEquals(1, hits);
    }

    private Map<String, String> createGenericParamsMap(String repoString) {
        Map<String, String> genericParams = new HashMap<>();
        genericParams.put(BuildConfigurationParameterKeys.EXTRA_REPOSITORIES.name(), repoString);
        return genericParams;
    }
}
