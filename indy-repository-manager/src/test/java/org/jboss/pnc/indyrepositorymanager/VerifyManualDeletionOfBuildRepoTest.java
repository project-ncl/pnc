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

import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.folo.client.IndyFoloContentClientModule;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@Category(ContainerTest.class)
public class VerifyManualDeletionOfBuildRepoTest extends AbstractImportTest {

    @Test
    public void manuallyPromoteBuildRepoToChainGroup() throws Exception {
        String path = "/org/myproj/myproj/1.0/myproj-1.0.pom";
        String content = "This is a test " + System.currentTimeMillis();

        String buildId = "build";

        // create a dummy non-chained build execution and a repo session based on it
        BuildExecution execution = new TestBuildExecution(buildId);
        RepositorySession session = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap());

        String pkgType = MAVEN_PKG_KEY;

        // simulate a build deploying a file.
        StoreKey hostedKey = new StoreKey(pkgType, StoreType.hosted, buildId);
        indy.module(IndyFoloContentClientModule.class)
                .store(buildId, hostedKey, path, new ByteArrayInputStream(content.getBytes()));

        // now, extract the build artifacts. This will trigger promotion of the build hosted repo to the chain group.
        RepositoryManagerResult result = session.extractBuildArtifacts(true);

        // do some sanity checks while we're here
        List<Artifact> deps = result.getBuiltArtifacts();
        assertThat(deps.size(), equalTo(1));

        Artifact a = deps.get(0);
        assertThat(a.getFilename(), equalTo(new File(path).getName()));

        // construct a dummy BuildRecord for use below
        BuildRecord record = new BuildRecord();
        record.setBuildContentId(buildId);

        // unset the readonly flag on the hosted repo to allow its deletion
        IndyStoresClientModule indyStoreAdmin = indy.stores();
        StoreKey key = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, record.getBuildContentId());
        HostedRepository store = indyStoreAdmin.load(key, HostedRepository.class);
        store.setReadonly(false);
        indyStoreAdmin.update(store, "Unsetting readonly-flag to allow deletion");

        // manually delete the build to the public group (since it's convenient)
        RunningRepositoryDeletion deletion = driver.deleteBuild(record, pkgType, accessToken);
        deletion.monitor(
                completed -> assertThat("Manual deletion failed.", completed.isSuccessful(), equalTo(true)),
                error -> {
                    error.printStackTrace();
                    fail("Failed to manually delete: " + error.getMessage());
                });

        // end result: the build hosted repo should no longer exist.
        assertThat(indyStoreAdmin.exists(hostedKey), equalTo(false));
    }

}
